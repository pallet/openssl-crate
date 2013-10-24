## Usage

The openssl crate provides a `server-spec` function that returns a
server-spec. This server spec will install openssl via system packages.

The `self-signed-cert` function can be used to generate a self signed cert.

The `generate-key` function can be used to create a rsa.

The `key-passphrase` function can be used to change the passphrase on an rsa key.

The `signing-request` function can be used to create a key signing request.

The `sign` function can be used to sign a certificate.

The `settings` function provides a plan function that should be called in the
`:settings` phase.  The function puts the configuration options into the pallet
session, where they can be found by the other crate functions, or by other
crates wanting to interact with openssl.

The `install` function is responsible for actually installing openssl.

## Live test on vmfest

For example, to run the live test on VMFest, using Ubuntu 12.04:

```sh
lein with-profile +vmfest pallet up --selectors ubuntu-13
lein with-profile +vmfest pallet down --selectors ubuntu-13
```
