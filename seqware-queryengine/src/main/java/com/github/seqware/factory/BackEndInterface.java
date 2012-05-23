/*
 * Copyright (C) 2012 SeqWare
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.github.seqware.factory;

import java.security.AccessControlException;

/**
 * 
 * @author dyuen
 */
public interface BackEndInterface {
    
    /**
     * Store obj in the back-end.
     * @param obj Object to be created
     * @throws AccessControlException if the user is not allowed to write to the 
     * parent object (i.e. create a Reference in a ReferenceSet without write 
     * permission to that ReferenceSet)
     */
    public void store(Object obj) throws AccessControlException;
    
    
    /**
     * Crawl through obj and update changes in the back-end.
     * @param obj Object to be updated in the back-end
     * @throws AccessControlException if the user does not have permission to
     * change this object
     * @return Due to copy-on-write, this can result in a new object that the 
     * user may wish to subsequently work on
     */
    public Object update(Object obj) throws AccessControlException;
    
    /**
     * Update the obj using the latest information from the back-end
     * @param obj Object to be refreshed from the back-end
     * @throws AccessControlException if the user has lost permission to 
     * read the object 
     * @return Due to copy-on-write, this may return a new object with 
     * updated information
     */
    public Object refresh(Object obj) throws AccessControlException;
    
    /**
     * Delete obj (will cascade in the case of sets to their 
     * children)
     * @param obj Object to be deleted from the back-end
     * @throws AccessControlException  if the user does not have permission to
     * delete this (or children) objects
     */
    public void delete(Object obj) throws AccessControlException;
}