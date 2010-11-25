(ns ^{:doc "Database layer for atom feeds" :author "Thomas Engelschmidt"}
  feeds.db
  (:require [clojure.contrib.sql :as sql]
            [clojure.contrib.logging :as logging]))

 (defn- connection-props [connect-string user password]
      {:classname "org.apache.derby.jdbc.EmbeddedDriver"
       :subprotocol "derby"
       :subname "feeder.derby" 
       :create true})

(defn- dev-feeder-props [user password]                       
     (connection-props "feeder" user password))
 
(def  feeder (dev-feeder-props "" ""))

 (defn clob-to-string [clob]
      "Turn a Derby 10.6.1.0 EmbedClob into a String"
      (with-open [rdr (java.io.BufferedReader. (.getCharacterStream clob))]
            (apply str (line-seq rdr))))
 
    (defn get-all [] 
      (sql/with-connection feeder 
         (sql/transaction
            (sql/with-query-results rs ["select * from atoms"] 
            (doseq [row rs] (clob-to-string (:atom row)))))))

 (defn insert-atom-entry
     "Insert data into the table"
     [resource,atom_]
   (sql/with-connection feeder
     (sql/insert-values
          :atoms
          [:resource :atom]
          [resource atom_])))


