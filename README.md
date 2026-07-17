# High Performance String Operations for Java (including mutable Strings)

Magic-Strings provides high performance string operation for Java. <br>
Key features are:

- <b>Performance:</b> Implementations of String operations are optimized for high performance.
Magic-Strings offers similar or better performance than the JDK or libraries like Guava or Commons-Lang (see benchmarks). 

- <b>Functionality</b>: Magic-Strings offer all the standard string operations like find, replace, remove, pad/truncate, wrap/unwrap,
split/join you expect from your string library.

- <b>Orthogonality</b>: Magic-Strings uses a unique orthogonal approach which lets you combine functionality and configuration
for each of your use cases. The approach is easy and powerful to use, no more looking for complex method names like prependIfMissingIgnoreCase().

- <b>Easy to use builders</b>: All string operations are provided by implementation classes configured through builders.
The builder use the functional approach with lambdas making them easy to use: <br>
StringReplacer.build(b -> b.replaceString(findString, replaceString))

- <b>Configurable support for Case Sensitivity</b>: All operations are available with configurable support for case sensitivity.
There is specialized implementation for case insensitivity which offers higher performance by ignoring some rare special cases. 

- <b>Full Unicode Support</b>: All operations are per default aware of surrogates to offer full Unicode support.
If you don't need full support, you can turn it off to get higher performance in turn.

- <b>Mutable Strings</b>: GapString is a high performance implementation of a mutable string. Using mutable strings allows
you to avoid the repeated creation of temporary string objects and achieve therefore higher performance. 

- <b>Support for CharSequence</b>: All string operations accept CharSequence as input saving you from creating temporary String objects
just for the sake of calling a method. 
