(ns ^{:doc "Database layer for atom feeds" :author "Thomas Engelschmidt"}
  feeds.db
  (:use feeds.config)
  (:require [clojure.contrib.sql :as sql]
            [clojure.contrib.logging :as logging]))


 
 (defn clob-to-string [clob]
      "Turn a Derby 10.6.1.0 EmbedClob into a String"
      (with-open [rdr (java.io.BufferedReader. (.getCharacterStream clob))]
            (apply str (line-seq rdr))))
 
(defn find-atom-entry [feed id] 
  (sql/with-connection (feed-db)
    (sql/transaction                        
     (sql/with-query-results rs ["select id, feed, created_at date, atom from atoms" ]  
        (conj  (first rs) {:atom (clob-to-string (:atom (first  rs)))} )))))

(defn find-atom-feed [feed day month year] 
  (let [date (str "{d '" year "-" month "-" day "'}")] 
    (sql/with-connection (feed-db)
      (sql/transaction
        (sql/with-query-results rs [(str "select id, feed, created_at date from atoms where feed = ? and created_at  = " date ) feed ]
              (vec rs))))))
                                ;         (map   #(conj  (first %) {:atom (clob-to-string (:atom (first  %)))} ) rs  ))))))
  
  
(defn insert-atom-entry
     "Insert data into the table"
     [feed, atom_]
   (sql/with-connection (feed-db)
    (logging/spy (sql/insert-values
          :atoms
          [:feed :atom]
          [feed atom_]))))


