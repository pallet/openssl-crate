[Repository](https://github.com/pallet/openssl-crate) &#xb7;
[Issues](https://github.com/pallet/openssl-crate/issues) &#xb7;
[API docs](http://palletops.com/openssl-crate/0.8/api) &#xb7;
[Annotated source](http://palletops.com/openssl-crate/0.8/annotated/uberdoc.html) &#xb7;
[Release Notes](https://github.com/pallet/openssl-crate/blob/develop/ReleaseNotes.md)

A [pallet](http://palletops.com/) crate to install and configure openssl.

### Dependency Information

```clj
:dependencies [[com.palletops/openssl-crate "0.8.0-alpha.1"]]
```

### Releases

<table>
<thead>
  <tr><th>Pallet</th><th>Crate Version</th><th>Repo</th><th>GroupId</th></tr>
</thead>
<tbody>
  <tr>
    <th>0.8.0-RC.4</th>
    <td>0.8.0-alpha.1</td>
    <td>clojars</td>
    <td>com.palletops</td>
    <td><a href='https://github.com/pallet/openssl-crate/blob/0.8.0-alpha.1/ReleaseNotes.md'>Release Notes</a></td>
    <td><a href='https://github.com/pallet/openssl-crate/blob/0.8.0-alpha.1/'>Source</a></td>
  </tr>
</tbody>
</table>

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

For example, to run the live test on VMFest, using Ubuntu 13:

```sh
lein with-profile +vmfest pallet up --selectors ubuntu-13
lein with-profile +vmfest pallet down --selectors ubuntu-13
```

## License

Copyright (C) 2012, 2013 Hugo Duncan

Distributed under the Eclipse Public License.
