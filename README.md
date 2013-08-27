# btc-e-client

A small client for interacting with the btc-e api. Works for the public and trading api.


## Usage

Example
```
  (require '[btc-e-client.core :as btce]) ;; load the library

  (btce/get-ticker) ;; Get the ticker

  (get-in (btce/get-ticker) [:ticker :avg]) ;; Get the average

  (btce/get-trades) ;; get the trades

  ;; The trade api's method names and params can be found at https://btc-e.com/api/documentation

  (btce/trade-api-request "TradeHistory") ;; Play around with the trade api
  (btce/trade-api-request "getInfo") ;; Get the user's Info

  (btce/trade-api-request "CancelOrder" {:order_id 123}) ;; Cancel an order with an argument

```

Functions: 
```
;; Public api functions
(get-ticker)
(get-trades)
(get-fee)
(get-depth)

;;Trade api functions
(trade-api-request method-name params?)

(async-trade-api-request method-name params?) ;;returns a promise to a response

```

Other api endpoints: Only the btc_usd is used in the public-api calls. You can however call any route you want with
```
(get-body-sync url)

;; Example get the ltc_usd ticker

(require '[btc-e-client.core :as btce]) ;; load the library
(btce/get-body-sync "https://btc-e.com/api/2/ltc_usd/ticker")
```


## License

Distributed under the Eclipse Public License, the same as Clojure.
