Amplafi-flow-core is an API framework that offers these benefits:

Needs:

===========================================================

First pass:

  Multi-step web entry:
     - mapping data to key value map
     - being able to resume or share work in progress.
         
  In progress changes not committed to the db.
  
  cancelled changes don't need to be rolled back.
  
  two separate users simulatenously editing don't see partial changes.
  
========================================================
 
Second pass:

 DRY - doing one thing and discovered that I want to share pieces from another flow.
Second cut:
   subtasks: 
      i.e. for example, a user is doing an operation -- check out - but now we need to interrupt to ask for credit card information if we don't have it.
      
   

Consistent serialization

Third cut: 

 1. Multi-step web entry:
    1. must store entered data including changes to existing objects *without* altering the database.
    1. all changes applied only when the user completes all the steps.
    1. allow user to refresh their browser
    1. allow a user to send a link to their in-progress changes so that someone else can help complete the changes. ( For example, a manager approving a vacation request ) 
    1. allow a user to logout and relogin to resume the work in progress.
 1. Data serialization of work-in-progress handled consistently by the framework.
 1. Security considerations:
    1. Type safety enforced. All parameters are typed and verified.
    1. Security attacks that could prevented with  

 1. Self-describing api calls and parameters
 1. Enforced read-only parameters ( so security issues in a api call (flow) do not allow for database changes )
 1. Enforced data visibility ( can internal parameters be seen? )
 1. Standard serialization mechanism and caching
 1. Clear transaction boundaries :
   1. force database changes to occur at a single defined point
   1. allow cached objects to be discarded.
 1. Composable flows from individual properties.


== Need (Conversational State Management) ==

 * Users typically have long running sessions. 
 * Websites will cache state in the user session in order to avoid expensive database queries. 
 * Web frameworks provide poor mechanisms to determine when to invalidate portions of the user session state.

As the page interactions become more complex it becomes difficult or impossible for a given page to know what is already cached, and what items cached
can be discarded. Often times websites discover that a user session size can grow to the multiple megabytes for no apparent reason.

Even so-called "single page" RIA websites face this issue. There is always some server-side session state:
 * data that cannot be shared with the client because:
   * the state is too large (search results), 
   * because there are security considerations (userids, passwords, authentication tokens),
   * potential privacy issues if it was intercepted ( tracking/usage data ) 
 * data that is sharable but must validated as unchanged by the client (userids, session keys),
 * data that represents in-progress customer effort that should be saved periodically just in case of a lost connection or client crash ( draft email )

 (in progress )
 * Problems with existing solutions
  * Existing frameworks are primarily controller frameworks and offer rich mechanism for describing how the user can navigate a website.
  * Existing frameworks assume many things about the users' method of interaction with the application. Trying to combine multiple frameworks,
such as Tapestry with Spring results in a tug-of-war between frameworks over which framework will provide the base javax.servlet implementation.
  * When adding new libraries to an existing code base, it is rarely possible to change which framework is controlling the servlet interaction. The corporate policy has standardized what the web framework will be and this is not subject to easy revision.
