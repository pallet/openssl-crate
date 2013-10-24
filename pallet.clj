;;; Pallet project configuration file

(require
 '[pallet.crate.openssl-test :refer [test-spec]]
 '[pallet.crates.test-nodes :refer [node-specs]])

(defproject openssl-crate
  :provider node-specs                  ; supported pallet nodes
  :groups [(group-spec "openssl-live-test"
             :extends [with-automated-admin-user
                       test-spec]
             :roles #{:live-test :default})])
