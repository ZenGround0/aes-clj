(ns aes-timing-attack.core
  (:require [aes-timing-attack.aes :as aes]
            [ring.adapter.jetty :as jetty]
            [ring.util.request :as ring-request]
            [compojure.core :refer [defroutes GET POST]]))

(defn foo
  "I don't do a whole lot."
  [x]
  (println x "Hello, World!"))

(defn foofoo
  [x]
  (aes/bar x))

(defroutes app-routes
  (GET "/00" [] (format "<h1>Hello, Clojure! encryption time: %s </h1>"
                      (time (aes/state-string
                       (aes/aes-enc (aes/hex->bytes "00000000000000000000000000000000")
                                    (aes/hex->bytes "00000000000000000000000000000000") )))))
  (GET "/11"
       [] (format "<h1>Hello, Clojure! encryption: %s </h1>"
                      (aes/state-string
                       (aes/aes-enc (aes/hex->bytes "11111111111111111111111111111111")
                                    (aes/hex->bytes "11111111111111111111111111111111") ))))
  (GET "/favicon.cio" [] "")
  (GET "/" [] "Hello")
  ;; body of http request is a hex encoded string of the plaintext
  (POST "/encrypt" request
        (let [ptext  (ring-request/body-string request)]
          (with-out-str(
                        time (aes/state-string (aes/aes-enc (aes/hex->bytes ptext)
                                         (aes/hex->bytes "00000000000000000000000000000000"))))))))

(defn -main [& args]
  (jetty/run-jetty app-routes {:port 3042}))
