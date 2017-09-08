# KotlinFirechat
This project is a basic chat application using Flux Architecture together with [Rx Java 2.0](https://github.com/ReactiveX/RxJava/tree/2.x), [Dagger2](https://google.github.io/dagger/) and [RxFirebase2](https://github.com/FrangSierra/Rx2Firebase).

All the Firebase login/creation acccounts logic is inside *SessionController* meanwhile the chat management is in the *ChatController*. Both are used by their respectives Store to change their respective states, using the real implementation or a fake one with testing purposes. In the other hand, the activities will be listening the changes of the stores to update themselves with the new data.

Flux is an Architecture which works pretty well with Firebase(It does aswell with Redux), it allows you to keep all the data in cache in a really easy mode, together with data persistence of Firebase it becomes a really strongh way of develop applications.

![alt tag](https://raw.githubusercontent.com/lgvalle/lgvalle.github.io/master/public/images/flux-graph-complete.png)
Graph by [Luis G. Valle](http://lgvalle.xyz/)

## Further Reading:
* [Facebook Flux Overview](https://facebook.github.io/flux/docs/overview.html)
* [Flux & Android](http://armueller.github.io/android/2015/03/29/flux-and-android.html)
* [Testing Flux Applications](https://facebook.github.io/flux/docs/testing-flux-applications.html#content)
* [Flux Step by Step](http://blogs.atlassian.com/2014/08/flux-architecture-step-by-step/)
* [What's different in Rx 2.0](https://github.com/ReactiveX/RxJava/wiki/What's-different-in-2.0)
* [Dependency Injection with Dagger 2](https://guides.codepath.com/android/Dependency-Injection-with-Dagger-2)
