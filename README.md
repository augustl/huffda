# huffda

An expectations based monitoring system.


## Timed expectations

Timed expectations lets you say things like:

* After a message is posted, we expect it to be processed by the delivery system within two minutes
* After a message is received, we expect it to be delivered to these five people within five minutes.

You post an expectations to huffda that looks like this:

```js
// Expectation
{
  "key": "product-updated-id-43872-version-4",
  "timeoutMs": 60000,
  "contexts": ["product-updated", "product-updated-id-43872", "product-updated-id-43872-version-4", "product-core"],
  "system": "main-webapp",
  "reason": "Product is expected to be processed by warehouse system"
}
```

The timeout tells us that we expect this expectation to be fulfilled within a minute.

The context is used to look up expectations later, so that you can easily show all expectations for a given user, or for that particular save by passing both the id and the version.

The system and the reason is metadata that is used for human consumption to better understand the nature of the expectation.

The expectations is typically created by a producer of some sort. The system that actually fulfills the expectation, posts a separate message when it is fulfilled:

```
// Fulfillment
{
  "key": "product-updated-id-43872-version-4",
  "ok": true,
  "reason": "User version 4 was processed by the warehouse system"
}
```

Huffda then lets you query for the state of your expectations, set up alerts when expectations are unfulfilled, and so on.

Huffda assumes that _all_ expectations are to be fulfilled. It does not support partial fulfillments (yet). So you can't expect things like "at least 3 of the 5 quxes should do baz".

## Threaded expectations

Threaded expectations were written to monitor queues over time, but it can be useful for many different things.

First, you need to configure Huffda to be aware of your threaded expectation. You tell Huffda where the URL is to your HTTP service that performs the checking. Huffda will call this webservice at the configured interval, and pass in the data from the previous call.

Here's how you would monitor a queue:

TODO: Describe threading setup


## License

Copyright © 2017 August Lilleaas
