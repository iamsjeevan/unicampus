import requests
from bs4 import BeautifulSoup
import pandas as pd
import time
import re # For cleaning numbers

# --- Configuration (Same as before) ---
BASE_URL = "https://www.moneycontrol.com/stocks/company_info/print_financials.php"
COMPANY_ID = "RI"
REPORT_TYPE = "balance_VI"
MAX_PAGES = 10
REQUEST_DELAY = 2
HEADERS = {
    "Accept": "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8",
    "Accept-Language": "en-US,en;q=0.6",
    "Connection": "keep-alive",
    "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/136.0.0.0 Safari/537.36",
    "Sec-Fetch-Dest": "iframe",
    "Sec-Fetch-Mode": "navigate",
    "Sec-Fetch-Site": "same-origin",
    "Sec-Fetch-User": "?1",
    "Sec-GPC": "1",
    "Upgrade-Insecure-Requests": "1",
}

# --- Helper Functions (Same as before) ---
def clean_value(value_str):
    if value_str is None: return None
    text = value_str.strip()
    if text == '-' or text == '': return None
    try: return float(text.replace(',', ''))
    except ValueError: return text

def get_form_params(soup):
    form = soup.find('form', {'name': 'finyear_frm'})
    if not form: return None
    params = {}
    for inp in form.find_all('input', {'type': 'hidden'}):
        params[inp.get('name')] = inp.get('value')
    return params

def parse_financial_table_from_soup(soup, company_name, page_num_for_debug=""):
    all_candidate_tables = soup.find_all('table', class_='table4', width="100%", cellspacing="0", cellpadding="0", bgcolor="#ffffff")
    financial_table = None
    for idx, candidate_table in enumerate(all_candidate_tables):
        first_few_rows = candidate_table.find_all('tr', limit=5)
        for tr_candidate in first_few_rows:
            tds = tr_candidate.find_all('td')
            if len(tds) > 1 and "detb" in tds[0].get('class', []) and \
               tds[1].text.strip().startswith("Mar ") and "detb" in tds[1].get('class', []):
                financial_table = candidate_table
                break
        if financial_table: break
            
    if not financial_table:
        print(f"Warning (Page {page_num_for_debug}): Could not identify financial data table for {company_name}.")
        return [], []

    rows = financial_table.find_all('tr')
    table_data = []
    column_headers = []
    header_found_flag = False
    for i, row in enumerate(rows):
        cols = row.find_all('td') 
        if not cols: continue
        if not header_found_flag and len(cols) > 1 and "detb" in cols[0].get('class', []) and cols[1].text.strip().startswith("Mar "):
            column_headers = ["Item"] + [col.text.strip() for col in cols[1:]]
            header_found_flag = True
            continue 
        if not header_found_flag: continue
        if len(cols) > 1 and "12 mths" in cols[1].text.strip() and ("det" in cols[1].get('class', []) or "detb" in cols[1].get('class', [])): continue
        if len(cols) == 1 and cols[0].get('colspan') and cols[0].find('img'): continue
        
        first_col_classes = cols[0].get('class', [])
        item_name = cols[0].text.replace('\xa0', ' ').strip()
        if "Source :" in item_name: break
        if not item_name and not any(c.text.strip() for c in cols[1:] if c.text is not None): continue

        is_section_header = "detb" in first_col_classes and \
                            (cols[0].get('colspan') == '2' or len(cols) < len(column_headers) -1 if column_headers else True) 
        
        if is_section_header and item_name:
            all_other_cells_empty_or_not_present = True
            if len(cols) > 1 and column_headers: # Check only if headers are defined
                if len(cols[1:]) < len(column_headers[1:]): pass
                else:
                    for k_idx, k_col_header in enumerate(column_headers[1:]):
                        if k_idx < len(cols[1:]) and cols[k_idx+1].text.strip() != "":
                            all_other_cells_empty_or_not_present = False; break
            if all_other_cells_empty_or_not_present:
                row_data = {"Item": item_name}
                if column_headers:
                    for ch in column_headers[1:]: row_data[ch] = None
                table_data.append(row_data); continue

        if item_name and column_headers: # Ensure headers are defined before processing data rows
            if len(cols[1:]) == len(column_headers[1:]):
                values = [clean_value(col.text) for col in cols[1:]]
                row_data = {"Item": item_name}
                for k, v in zip(column_headers[1:], values): row_data[k] = v
                table_data.append(row_data)
            elif item_name and len(cols) <= 2 and "detb" in first_col_classes:
                row_data = {"Item": item_name}
                for ch in column_headers[1:]: row_data[ch] = None
                table_data.append(row_data)
    return table_data, column_headers

# --- Main Script with MERGING Logic ---
def main():
    all_pages_data_dictionaries = []
    master_year_headers = set() # To collect all unique 'Mar XX' headers

    session = requests.Session()
    session.headers.update(HEADERS)

    print(f"Fetching initial page for {COMPANY_ID}...")
    initial_params_get = {'sc_did': COMPANY_ID, 'type': REPORT_TYPE}
    try:
        response = session.get(BASE_URL, params=initial_params_get, timeout=20)
        response.raise_for_status()
    except requests.RequestException as e:
        print(f"Error fetching initial page: {e}")
        return

    current_page_html = response.text
    page_count = 1

    while page_count <= MAX_PAGES:
        print(f"\n--- Processing Page {page_count} ---")
        soup = BeautifulSoup(current_page_html, 'lxml')
        company_name_tag = soup.find('td', class_='det')
        company_name = COMPANY_ID
        if company_name_tag and company_name_tag.b: company_name = company_name_tag.b.text.strip()
        elif company_name_tag: company_name = company_name_tag.text.strip()
        print(f"Company: {company_name}")

        page_table_data, page_headers = parse_financial_table_from_soup(soup, company_name, str(page_count))
        
        if page_table_data:
            print(f"Extracted {len(page_table_data)} rows of data from page {page_count}.")
            if page_headers and len(page_headers) > 1:
                for header in page_headers[1:]: # Skip "Item"
                    if header.startswith("Mar "): # Ensure it's a year header
                        master_year_headers.add(header)
            
            for row_dict in page_table_data:
                # We don't need PageSource per row if we merge by Item
                all_pages_data_dictionaries.append(row_dict)
        else:
            print(f"No data extracted from page {page_count}.")

        prev_years_link = soup.find('a', onclick=lambda x: x and "post_prevnext('2')" in x)
        if prev_years_link:
            print("Found 'Previous Years' link...")
            form_params = get_form_params(soup)
            if not form_params: print("No form params. Stopping."); break
            form_params['nav'] = 'next' 
            post_referer = response.url
            post_headers = HEADERS.copy()
            post_headers.update({'Referer': post_referer, 'Origin': "https://www.moneycontrol.com", 'Content-Type': 'application/x-www-form-urlencoded'})
            time.sleep(REQUEST_DELAY)
            try:
                response = session.post(BASE_URL, data=form_params, headers=post_headers, timeout=20)
                response.raise_for_status()
                current_page_html = response.text
                page_count += 1
            except requests.RequestException as e:
                print(f"Error fetching next page (POST): {e}"); break
        else:
            print("No 'Previous Years' link. End of pagination."); break
            
    if all_pages_data_dictionaries:
        print(f"\n--- Merging data for {len(all_pages_data_dictionaries)} total rows collected ---")
        
        merged_items_data = {} # Key: Item name, Value: dict of year data
        
        # First pass: Collect all data for each item
        for row_dict in all_pages_data_dictionaries:
            item_name = row_dict.get("Item")
            if not item_name:
                continue

            if item_name not in merged_items_data:
                merged_items_data[item_name] = {"Item": item_name} # Initialize with Item name
            
            # Update with year values from this row_dict
            # Only update if the value is not None (i.e., actual data)
            for key, value in row_dict.items():
                if key.startswith("Mar ") and value is not None:
                    # If the year already exists and has a value, we might have a conflict
                    # or it's a section header being overwritten.
                    # For financial items, values should be unique per item-year.
                    # Section headers will have all year values as None from parsing.
                    if merged_items_data[item_name].get(key) is None or value is not None:
                         merged_items_data[item_name][key] = value
                elif key == "Item": # Already handled
                    pass
                elif value is not None : # For other potential non-year columns, if any
                    if merged_items_data[item_name].get(key) is None:
                        merged_items_data[item_name][key] = value


        # Convert the merged_items_data dictionary to a list of dictionaries for DataFrame
        final_merged_list = list(merged_items_data.values())
        
        # Define final column order
        sorted_year_headers = sorted(list(master_year_headers), reverse=True)
        final_ordered_columns = ["Item"] + sorted_year_headers
        
        # Add any other non-Item, non-Year columns that might have been collected (if any)
        # to the end of final_ordered_columns
        all_collected_keys = set()
        for item_data in final_merged_list:
            all_collected_keys.update(item_data.keys())
        
        other_columns = [key for key in all_collected_keys if key not in final_ordered_columns]
        final_ordered_columns.extend(other_columns)


        print(f"Final columns for DataFrame: {final_ordered_columns}")

        df = pd.DataFrame(final_merged_list)
        
        # Ensure all specified columns exist in the DataFrame, adding them with NaNs if missing
        for col in final_ordered_columns:
            if col not in df.columns:
                df[col] = pd.NA # Use pd.NA for consistency with missing values
        
        df = df[final_ordered_columns] # Enforce column order

        print("\n--- Merged Financial Data (Sample) ---")
        pd.set_option('display.max_rows', 20)
        pd.set_option('display.max_columns', None)
        pd.set_option('display.width', 1200)
        print(df.head(10))
        if len(df) > 10: print("..."); print(df.tail(10))
        print(f"\nDataFrame shape: {df.shape}")

        csv_filename = f"{COMPANY_ID}_{REPORT_TYPE}_merged_single_row_per_item.csv"
        df.to_csv(csv_filename, index=False, encoding='utf-8-sig')
        print(f"\nMerged data saved to {csv_filename}")
    else:
        print("No data was extracted.")

if __name__ == "__main__":
    main()