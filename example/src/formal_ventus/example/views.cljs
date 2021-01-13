(ns formal-ventus.example.views
  (:require [formal-ventus.core :as fv]))

(def sample-schema
  [:map
   [:name {:placeholder "Name"
           :default-value "tom3"
           :optional true}
    [:string {:min 0 :max 100}]]
   [:description {:placeholder "Description"}
    string?]
   [:enabled {:default-value true
              :label "Enabled"}
    boolean?]
   [:age {:placeholder "Age"} integer?]
   [:fruit [:sequential string?]]
   [:config {:default-value {:foo "foo"}}
    [:map
     [:foo {:label "Foo Input"
            :placeholder "foo"}
      :string]
     [:bar {:label "Bar Input"
            :placeholder "bar"}
      :string]]]])

(defn main-panel
  []
  [:div {:class-name "w-full max-w-md p-8"}
   [fv/form
    {:schema sample-schema
     :on-change #(cljs.pprint/pprint {:sample %})}]])
