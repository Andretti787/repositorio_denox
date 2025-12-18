from werkzeug.security import generate_password_hash

your_plain_text_password = "@Mb202512" # ¡Cámbiala por tu contraseña real!
hashed_password = generate_password_hash(your_plain_text_password)
print('passwd: ' + hashed_password)
