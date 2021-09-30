(ns servico-clojure.server
  (:require [io.pedestal.http.route :as route]
            [io.pedestal.http :as http]
            [io.pedestal.test :as test]
            [servico-clojure.database :as database]))


(defn funcao-hello [request]
  {:status 200
   :body   (str "Hello World " (get-in request [:query-params :name] "Everybody"))})

(defn criar-tarefa [request]
  (let [uuid (java.util.UUID/randomUUID)
        nome (get-in request [:query-params :nome])
        status (get-in request [:query-params :status])
        tarefa {:uuid uuid :nome nome :status status}
        store (:store request)]
    (swap! store assoc uuid tarefa)
    {:status 200
     :body   tarefa}))

(defn consultar-tarefas [request]
  (let [store (:store request)]
    (println store)
    {:status 200 :body @store}))

(defn assoc-store [context]
  (update context :request assoc :store database/store))

(def db-interceptor
  {:name :db-interceptor
   :enter assoc-store})

(def routes (route/expand-routes
              #{["/hello" :get funcao-hello :route-name :hello-world]
                ["/tarefa" :post [db-interceptor criar-tarefa] :route-name :criar-tarefa]
                ["/consultar-tarefas" :get [db-interceptor consultar-tarefas] :route-name :consultar-tarefas]}))

(def service-map {::http/routes routes
                  ::http/port   9999
                  ::http/type   :jetty
                  ::http/join?  false})

(def server (atom nil))

(defn start-server []
  (reset! server (http/start (http/create-server service-map))))

(defn stop-server []
  (http/stop @server))

(defn restart-server []
  (stop-server)
  (start-server))

(start-server)

(defn test-request
  [verb url]
  (test/response-for (::http/service-fn @server) verb url))

(test-request :get "/hello")
(test-request :get "/hello?name=Bruno")
(test-request :post "/tarefa?nome=Ler&status=Pending")
(test-request :get "/consultar-tarefas")

(println "Starting server...")
