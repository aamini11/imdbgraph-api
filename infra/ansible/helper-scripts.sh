# View encrypted file
ansible-vault view secrets.enc --vault-password-file password.txt

# Encrypt
ansible-vault encrypt secrets.enc --vault-password-file password.txt

# Decrypt
ansible-vault decrypt secrets.enc --vault-password-file password.txt