(ns servico-clojure.server
  (:require [io.pedestal.http.route :as route]
            [io.pedestal.http :as http]
            [io.pedestal.test :as test]))

(def store (atom {}))

(defn funcao-hello [request]
  {:status 200
   :body   (str "Hello World " (get-in request [:query-params :name] "Everybody"))})

(defn criar-tarefa [request]
  (let [uuid (java.util.UUID/randomUUID)
        nome (get-in request [:query-params :nome])
        status (get-in request [:query-params :status])
        tarefa {:uuid uuid :nome nome :status status}]
    (swap! store assoc uuid tarefa)
    {:status 200
     :body   tarefa}))

(defn consultar-tarefas [_]
  (let [formatted-atom @store]
    {:status 200 :body formatted-atom}))

(def routes (route/expand-routes
              #{["/hello" :get funcao-hello :route-name :hello-world]
                ["/tarefa" :post criar-tarefa :route-name :criar-tarefa]
                ["/consultar-tarefas" :get consultar-tarefas :route-name :consultar-tarefas]}))

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
(clojure.pprint/pprint (test-request :post "/tarefa?nome=Ler&status=Pending"))
(clojure.pprint/pprint (test-request :get "/consultar-tarefas"))

(println "Starting server...")
