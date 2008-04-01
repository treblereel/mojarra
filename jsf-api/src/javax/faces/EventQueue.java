/*
 * $Id: EventQueue.java,v 1.5 2002/04/05 19:40:15 jvisvanathan Exp $
 */

/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package javax.faces;

import java.util.EventObject;
import java.util.Iterator;

/**
 * The class which provides a queue for posting and retrieving
 * events generated by JavaServer Faces.  The model for the queue
 * is first-in/first-out.  
 * <p>
 * There is generally one EventQueue instance per session and
 * is available from the event context object.
 * @see FacesContext#getEventQueue
 * <p>
 * During the event-processing phase of a request cycle in that session, 
 * any events which are encoded in the request (i.e. originated on 
 * the client) are encapsulated in Event objects and posted on the event 
 * queue. Events may also be posted to the event queue from other parts of
 * the application.
 * <p>
 * This class is thread-safe; only one thread is permitted to 
 * modify the queue at a time.  The queue is modified by either
 * adding an event using <code>add(Event)</code>, removing an
 * event using <code>remove(Event)</code> or <code>removeNext()</code>,
 * or removing all events using <code>clear()</code>.  If no threads
 * are modifying the queue, multiple threads may have read access, 
 * using <code>peekNext()</code> to look at the next event or
 * <code>isEmpty()</code> to check for an empty queue.
 * <p>
 */
public class EventQueue {

    /**
     * Places an event object on the event queue.  
     * @param e the Event object being placed on the event queue
     */
    public void add(EventObject e) {}

    /**
     * Removes all event objects from the event queue.
     */
    public void clear() {}

    /**
     * @return Boolean object indicating whether or not there are
     *         event objects in the event queue.
     */
    public boolean isEmpty() {
        return false;
    }

    /**
     * Removes the specified event object from the event queue
     * if it is present.
     * @param e the event object to be removed from the event queue
     */
    public void remove(EventObject e) {}

    /**
     * Returns the next event on the event queue.
     * Returns null if there are no more events on
     * the event queue.
     * @return Event object representing the next event in the queue
     *         or null if there are no more events on the queue
     */
     public EventObject getNext() {
         return null;
     }

    /**
     * Returns the next event on the event queue, but does not
     * remove it from the queue.  Returns null if there are no more 
     * events on the event queue.
     * @return Event object representing the next event in the queue
     *         or null if there are no more events on the queue
     */
     public EventObject peekNext() {
         return null;
     }

    /**
     * Returns an iterator of events in the queue.
     * @return Iterator object representing all events 
     *         on the queue
     */
     public Iterator iterator() {
         return null;
    }

}


    
