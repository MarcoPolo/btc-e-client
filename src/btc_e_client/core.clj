(ns btc-e-client.core
  "Client library for accessing the btc-e api.
   Needs your secret and key inside environment variables called BTC_KEY and BTC_SECRET"
  (:use pandect.core)
  (:require [clojure.data.json :as json]
            [org.httpkit.client :as http]
            clojure.walk))

(defn init
  "Initiates an api structure which can be used to interact with the api.
  Currency is the currencies you want to interact in, for example :btc_usd,
  :ltc-usd, etc... You can leave the secret and api-key as empty strings if you
  just want to use the public api"
  [currency secret api-key ]
  { :curr currency :secret secret :key api-key})

(def default-api (init :btc_usd "" ""))

(def endpoint "https://btc-e.com/tapi")

(defn- public-api
  [api endpt]
  (str "https://btc-e.com/api/2/" (:curr api) "/" (name endpt)))

(defn- get-body-sync [url]
  (json/read-str (:body @(http/get url)) :key-fn keyword))

(defn get-ticker
  ([] (get-ticker default-api))
  ([api] (get-body-sync (public-api api :ticker))))

(defn get-trades
  ([] (get-trades default-api))
  ([api] (get-body-sync (public-api api :trades))))

(defn get-fee
  ([] (get-fee default-api))
  ([api] (get-body-sync (public-api api :fee))))

(defn get-depth
  ([] (get-depth default-api))
  ([api] (get-body-sync (public-api api :depth))))

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

(defn- btc-e-request-raw [api post-data]
  ;; add the nonce to the data
  (let [post-data (str post-data "&nonce=" (int (/ (System/currentTimeMillis) 1000)))]
    (http/post endpoint {:headers {"Key" (:key api)
                                   "Sign" (sha512-hmac post-data (:secret api))
                                   "Content-Type" "application/x-www-form-urlencoded"}
                         :body post-data})))

(defn async-trade-api-request
  "Call the btc-e api"
  ([api method-name params]
   (btc-e-request-raw api (postage (assoc params "method" method-name))))
  ([api method-name]
   (async-trade-api-request api method-name {})))

(defn trade-api-request
  "Synchronous version to call the api request"
  ([api method-name params]
    (json/read-str
      (:body @(async-trade-api-request api method-name params))
      :key-fn keyword))
  ([method-name]
   (trade-api-request api method-name {})))

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


