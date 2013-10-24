(ns pallet.crate.openssl-test
  (:require
   [clojure.test :refer :all]
   [clojure.tools.logging :as logging]
   [pallet.actions :refer [exec-checked-script]]
   [pallet.api :refer [plan-fn server-spec]]
   [pallet.build-actions :as build-actions :refer [build-actions]]
   [pallet.crate.openssl :as openssl]))

(deftest invoke-test
  (is (build-actions {}
        (openssl/settings {})
        (openssl/install))))

(def test-spec
  (server-spec
   :extends [(openssl/server-spec {})]
   :phases {:generate (plan-fn
                        (openssl/self-signed-cert "server.key" "server.crt"))
            :verify (plan-fn
                      (exec-checked-script
                       "Verify files"
                       (set! rv 0)
                       (when (not (file-exists? "server.key"))
                         (println "Key did not exist")
                         (set! rv 1))
                       (when (not (file-exists? "server.csr"))
                         (println "CSR did not exist")
                         (set! rv 1))
                       (when (not (file-exists? "server.crt"))
                         (println "CRT did not exist")
                         (set! rv 1))

                       ("exit" @rv)))}
   :default-phases [:install :configure :generate :verify]))
