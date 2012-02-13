# ciid-tweeq

This project was developed as part of the example problem domain during the Introduction to Programming workshop at CIID Copenhagen in the 2nd week of February 2012.

CIID Tweeq is a very basic Twitter like platform built in Clojure, Noir and CouchDB. The concept allowed us to address several fundamental topics of programming and the choice of tools gave us the opportunity to interactively experiment with them. In addition to sending & filtering messages, the project was meant to support the concept of action triggers, which would be fired when a certain (or multiple) regular expression patterns have been found in a tweet. If a trigger fires, it would then forward the message to a specific HTTP POST endpoint and so have CIID Tweeq act as a simple device independent event distribution system. Alas we didn't have enough time to implement the triggers and focused instead on working with the collected data/tweets to discuss data analysis topics and produce a (simple visualization project)[http://github.com/postspectacular/ciid-tweeq-viz] in Processing.

## Usage

```bash
lein deps
lein run
```

TBC...

## License

Copyright (C) 2012 Karsten Schmidt

Distributed under the Eclipse Public License, the same as Clojure.
