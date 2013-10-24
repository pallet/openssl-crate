(ns pallet.crate.openssl
  "Install and configure openssl"
  (:require
   [clojure.java.io :refer [file]]
   [clojure.string :as string :refer [upper-case]]
   [clojure.tools.logging :refer [debugf]]
   [pallet.action :refer [with-action-options]]
   [pallet.actions :refer [exec-checked-script remote-file]]
   [pallet.api :as api :refer [plan-fn]]
   [pallet.crate :refer [assoc-settings defmethod-plan defplan get-settings]]
   [pallet.crate-install :as crate-install]
   [pallet.script.lib :refer [cp make-temp-file mv rm]]
   [pallet.utils :refer [apply-map deep-merge maybe-assoc]]
   [pallet.version-dispatch
    :refer [defmethod-version-plan defmulti-version-plan]]
   [pallet.versions :refer [version-string as-version-vector]]))

(def facility :openssl)

(def static-defaults
  {:version "1.0.1"})

;;; # Settings
;;;
;;; We install from package manager, as os's are often openssl dependent

(defmulti-version-plan default-settings [version])

;; Download seems to be the only install method
(defmethod-version-plan
  default-settings {:os :os}
  [os os-version version]
  {:version (version-string version)
   :install-strategy :packages
   :packages ["openssl"]})


;;; ## Settings
(defn settings
  "Capture settings for openssl"
  [{:keys [version instance-id]
    :or {version (:version static-defaults)}
    :as settings}]
  (let [settings (deep-merge static-defaults
                             (default-settings version)
                             (dissoc settings :instance-id))]
    (debugf "openssl settings %s" settings)
    (assoc-settings facility settings {:instance-id instance-id})))

;;; # Install
(defplan install
  [{:keys [instance-id]}]
  (crate-install/install facility instance-id))

(defn generate-key
  "Create a private key with genrsa."
  [key-path & {:keys [key-size encryption pass]
               :or {key-size 2048 encryption :des3}}]
  (exec-checked-script
   "Create Private Key"
   ("openssl" genrsa
    (str "-" ~(name encryption))
    -passout (str "pass:" ~pass)
    -out ~key-path
    ~key-size)))

(defn key-passphrase
  "Set (or remove) the passphrase from a key"
  [key-path & {:keys [passin passout]}]
  (exec-checked-script
   "Change Key Passphrase"
   (set! tmpfile (make-temp-file keyfile))
   (cp ~key-path @tmpfile)
   ("openssl" rsa
    -in @tmpfile
    -out ~key-path
    ~(if passin (str "-passin pass:" passin) "")
    ~(if passout (str "-passout pass:" passout) ""))))

(def long-names
  {:c "countryName"
   :st "stateName"
   :l "localityName"
   :cn "commonName"
   :o "organizationName"
   :ou "organizationalUnitName"})

(def default-dn
  {:cn "Mark Smith"
   :ou "Java"
   :o "Sun"
   :l "Cupertino"
   :st "California"
   :c "US"})

(defn format-csr-config
  [m]
  (str "RANDFILE               = $ENV::HOME/.rnd

 [req]
 distinguished_name     = req_distinguished_name
 attributes             = req_attributes
 prompt                 = no

 [req_distinguished_name]\n"
       (string/join \newline
                    (map
                     (fn [k]
                       (when-let [v (k m)]
                         (str (upper-case (name k)) " = "
                              (if (or (#{:cn :o :ou} k)
                                      (.contains v " "))
                                (str \" v \")
                                v))))
                     (keys long-names)))
"\n[req_attributes]\n"))

(defn signing-request
  "Create a key signing request."
  [key-path csr-path & {:keys [cn ou o l s c passin passout]
                        :as options}]
  ;; default dname values taken from keytool manpage
  (remote-file
   "csr-config"
   :content (format-csr-config
             (merge default-dn (dissoc options :passin :passout)))
   :literal true)
  (exec-checked-script
   "Create Private Key"
   ("openssl" req
    -batch -new
    -key ~key-path
    -out ~csr-path
    ~(if passin (str "-passin pass:" passin) "")
    ~(if passout (str "-passout pass:" passout) "")
    -config "csr-config")))


(defn sign
  [key-path csr-path cert-path & {:keys [days]
                                  :or {days 365}}]
  (exec-checked-script
   "Sign Certificate"
   ("openssl" x509 -req -days ~days
    -in ~csr-path -signkey ~key-path -out ~cert-path)))

(defn self-signed-cert
  "Create a self signed certificate, with no passphrase on the key."
  [key-path cert-path & {:keys [cn ou o l s c]
                         :as options}]
  (generate-key key-path :pass "pwd")
  (apply-map
   signing-request key-path "server.csr" :passin "pwd"
   options)
  (key-passphrase key-path :passin "pwd")
  (sign key-path "server.csr" cert-path))

;;; # Server spec
(defn server-spec
  "Returns a service-spec for installing openssl."
  [{:keys [instance-id] :as settings}]
  (api/server-spec
   :phases {:settings (plan-fn
                        (pallet.crate.openssl/settings settings))
            :install (plan-fn
                       (install {:instance-id instance-id}))}))
