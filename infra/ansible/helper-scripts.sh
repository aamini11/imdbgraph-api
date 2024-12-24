# View encrypted file
ansible-vault view secrets.enc --vault-password-file secrets.pass

# Encrypt
ansible-vault encrypt secrets.enc --vault-password-file secrets.pass

# Decrypt
ansible-vault decrypt secrets.enc --vault-password-file secrets.pass