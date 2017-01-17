# svedn

> Reminder that the most successful and productive programming 
> environment ever invented is the spreadsheet.
> 
> Design matters.
>
> -- Reginald Braithwaite

A Clojure library providing tools for working with an amalgam of tabular data and [Extended Data Notation]() (EDN).  The name SVEDN stands for "**S**eparated **V**ariables with **EDN**.

## Usage

Don't. Yet.

## Why?

The reason for this library is twofold:

 1. I've been creating a lot of CSV data lately for personal projects.
    Over time the data became and amalgam of CSV and EDN and as a 
	result I've built a suite of ad-hoc tools.  This is an attempt to
	put those tools out in the world just in case someone else finds
	this approach useful.
	
 2. This is also an experiment in using clojure.core.spec to aide in
    parsing.

## More

I'd like to add the following:

 - Refactor the metadata / amendments handling code

 - Ammendments: Also accept a map instead of a column key

 - Metadata: Also accept a map instead of a column key

## Thanks

Thanks to David Chelimsky for creating the name Svedn.

## License

Copyright © 2016 Fogus

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
