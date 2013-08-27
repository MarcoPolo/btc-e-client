(ns btc-e-client.core
  "Client library for accessing the btc-e api.
   Needs your secret and key inside environment variables called BTC_KEY and BTC_SECRET"
  (:use pandect.core)
  (:require [clojure.data.json :as json]
            [org.httpkit.client :as http]
            clojure.walk))

(def secret (System/getenv "BTC_SECRET"))
(def api-key (System/getenv "BTC_KEY"))

(def endpoint "https://btc-e.com/tapi")

(def public-api {:ticker "https://btc-e.com/api/2/btc_usd/ticker"
                 :trades "https://btc-e.com/api/2/btc_usd/trades"
                 :fee "https://btc-e.com/api/2/btc_usd/fee"
                 :depth "https://btc-e.com/api/2/btc_usd/depth"})


(defn get-body-sync [url]
  (json/read-str (:body @(http/get url)) :key-fn keyword))

(defn get-ticker [] (get-body-sync (:ticker public-api)))

(defn get-trades [] (get-body-sync (:trades public-api)))

(defn get-fee [] (get-body-sync (:fee public-api)))

(defn get-depth [] (get-body-sync (:depth public-api)))



;; This is how their api works

;; HTTP headers:
;; key : api-key
;; sign : the HMAC sig: post data signed by secret using HMAC-SHA512

;; Endpoint
;; https://btc-e.com/tapi

;; POST Data
;; nonce : a nonce, using the unix ts in ms
;; method : method name
;; param : val
;; param2 : val2 
;; ...

(defn- postage 
  "Creates a post body"
  [param-map]
  (->> param-map
    (clojure.walk/stringify-keys)
    (reduce #(str %1 "&" (first %2) "=" (second %2)) "")
    (#(subs % 1)))) ;; Get rid of the first "&" in "&param1=stuff"

(defn- btc-e-request-raw [post-data]
  ;; add the nonce to the data
  (let [post-data (str post-data "&nonce=" (int (/ (System/currentTimeMillis) 1000)))]
    (http/post endpoint {:headers {"Key" api-key 
                                   "Sign" (sha512-hmac post-data secret) 
                                   "Content-Type" "application/x-www-form-urlencoded"}
                         :body post-data})))

(defn async-trade-api-request 
  "Call the btc-e api"
  ([method-name params]
   (btc-e-request-raw (postage (assoc params "method" method-name))))
  ([method-name]
   (async-trade-api-request method-name {})))

(defn trade-api-request
  "Synchronous version to call the api request"
  ([method-name params]
   (json/read-str (:body @(async-trade-api-request method-name params)) :key-fn keyword))
  ([method-name]
   (trade-api-request method-name {})))

;; Demo
(comment

  
  (require '[btc-e-client.core :as btce]) ;; load the library

  (btce/get-ticker) ;; Get the ticker

  (get-in (btce/get-ticker) [:ticker :avg]) ;; Get the average

  (btce/get-trades) ;; get the trades

  ;; The trade api's method names and params can be found at https://btc-e.com/api/documentation

  (btce/trade-api-request "TradeHistory") ;; Play around with the trade api
  (btce/trade-api-request "getInfo") ;; Get the user's Info

  (btce/trade-api-request "CancelOrder" {:order_id 123}) ;; Cancel an order with an argument

  )


