(ns riemann.elasticsearch-test
  (:use riemann.elasticsearch
        clojure.test)
  (:require [clj-http.client :as http]
            [cheshire.core :as json]
            [riemann.test-utils :refer [with-mock]]
            [riemann.logging :as logging]))

(logging/init)

(def ^:const input-event  {:description "test description"
                           :host "testhost"
                           :service "testservice"
                           :metric 1337
                           :state "leet"
                           :time 1451606400})
(def ^:const output-event {:host "testhost"
                           :service "testservice"
                           :metric 1337
                           :state "leet"
                           :tags nil
                           ":@timestamp" "2016-01-01T00:00:00.000Z"})

(deftest ^:elasticsearch elasticsearch-default-test
  (with-mock [calls clj-http.client/post]
    (let [elastic (elasticsearch {})
          json-event (json/generate-string output-event)]

      (testing "correct event reformatting and default post"
        (elastic input-event)
        (is (= (last @calls)
               ["http://127.0.0.1:9200/riemann-2016.01.01/event"
                {:body json-event
                 :content-type :json
                 :conn-timeout 5000
                 :socket-timeout 5000
                 :throw-entire-message? true}]))))))

(deftest ^:elasticsearch elasticsearch-opts-test
  (with-mock [calls clj-http.client/post]
    (let [elastic (elasticsearch {:es-endpoint "https://example-elastic.com"
                                  :es-index "test-riemann"
                                  :index-suffix "-yyyy.MM"
                                  :type "test-type"})
          json-event (json/generate-string output-event)]

      (testing "correct index/type formatting with custom elasticsearch opts"
        (elastic input-event)
        (is (= (last @calls)
               ["https://example-elastic.com/test-riemann-2016.01/test-type"
                {:body json-event
                 :content-type :json
                 :conn-timeout 5000
                 :socket-timeout 5000
                 :throw-entire-message? true}]))))))
