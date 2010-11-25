(ns jetty
  (:use feeds.routes
    ring.adapter.jetty
    )
  )


(defn boot []
  (future (run-jetty #'app {:port 8080}))
  )
