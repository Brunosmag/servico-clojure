(ns servico-clojure.server
  (:require [io.pedestal.http.route :as route]
            [io.pedestal.http :as http]
            [io.pedestal.test :as test]))

(defn funcao-hello [request]
  {:status 200
   :body (str "Hello World " (get-in request [:query-params :name] "Everybody"))})

(def routes (route/expand-routes
              #{["/hello" :get funcao-hello :route-name :hello-world]}))

(def service-map {::http/routes routes
                  ::http/port   9999
                  ::http/type   :jetty
                  ::http/join?  false})

(def server (atom nil))
(reset! server (http/start (http/create-server service-map)))

(defn test-request
  [verb url]
  (test/response-for (::http/service-fn @server) verb url))

(clojure.pprint/pprint (test-request :get "/hello"))
(clojure.pprint/pprint (test-request :get "/hello?name=Bruno"))

(println "Starting server...")
