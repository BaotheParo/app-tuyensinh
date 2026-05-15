import pandas as pd
import sys
import io

sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8')

try:
    df = pd.read_excel(r"d:\Project CV\app-tuyensinh\docs\Uu tien xet tuyen.xlsx")
    print(df.head(10).to_string())
    print("Columns:", df.columns.tolist())
except Exception as e:
    print("Error:", e)
