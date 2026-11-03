-- La contraseña de impresoras se guarda cifrada (AES-256-GCM, prefijo "enc:v1:"): el texto cifrado es más largo que el original.
ALTER TABLE impresoras ALTER COLUMN password TYPE varchar(512);
