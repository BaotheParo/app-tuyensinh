import mysql.connector

try:
    conn = mysql.connector.connect(
        host="localhost",
        user="root",
        password="123456",
        database="sgu_tuyensinh_2026"
    )
    cursor = conn.cursor()
    cursor.execute("ALTER TABLE xt_nguyenvongxettuyen ADD COLUMN is_tuyen_thang BOOLEAN DEFAULT FALSE")
    conn.commit()
    print("Success")
except Exception as e:
    print(e)
finally:
    if 'conn' in locals() and conn.is_connected():
        cursor.close()
        conn.close()
