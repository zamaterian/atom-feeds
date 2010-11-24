(println (str "Running init.clj"))
(use 'kunde.test.jetty) (boot)

(use 'lazytest.watch)

(defn go [] (start ["src/test/clojure/"]))

(use 'lazytest.watch)

(defn go [] (start ["src/test/clojure/"]))


(defn- get-classpath []
  (sort (map (memfn getPath)
    (seq (.getURLs (java.lang.ClassLoader/getSystemClassLoader))))))

(defn print-classpath []
  (print (get-classpath)))

(defn get-current-directory []
  (. (java.io.File. ".") getCanonicalPath))


(println (get-current-directory))
(print-classpath)

