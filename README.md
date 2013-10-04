# btc-e-client

A small client for interacting with the btc-e api. Works for the public and
trading api.

## Usage

Example
```clojure
;; load the library
(require '[btc-e-client.core :as btce])

;; instantiate your api information
(def api (btce/init :btc_usd "mysecret" "mykey"))

(btce/get-ticker api) ;; Get the ticker
(btce/get-ticker) ;; By default public api methods use btc_usd

(get-in (btce/get-ticker) [:ticker :avg]) ;; Get the average

(btce/get-trades) ;; get the trades

;; The trade api's method names and params can be found at
;; https://btc-e.com/api/documentation
;;
;; trade-api-request and async-trade-api-request must take in an api, since
;; they need the api key/secret

;; Play around with the trade api
(btce/trade-api-request api "TradeHistory")

;; Get the user's Info
(btce/trade-api-request api "getInfo")

;; Cancel an order with an argument
(btce/trade-api-request api "CancelOrder" {:order_id 123})

```

Functions:
```clojure
;; Init
(init :ltc_usd "mysecret" "mykey")

;; secret and key can be ommitted if only using public methods

;; Public api functions
(get-ticker)
(get-trades)
(get-fee)
(get-depth)

;;Trade api functions
(trade-api-request method-name params?)

;;returns a promise to a response
(async-trade-api-request method-name params?)

```

## License

Distributed under the Eclipse Public License, the same as Clojure.
