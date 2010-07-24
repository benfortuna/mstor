/**
 * This file is part of Base Modules.
 *
 * Copyright (c) 2010, Ben Fortuna [fortuna@micronode.com]
 *
 * Base Modules is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Base Modules is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Base Modules.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.fortuna.mstor.search

import java.util.Map;

import javax.mail.search.AndTerm;
import javax.mail.search.OrTerm;
import javax.mail.search.SearchTerm;
import javax.swing.JSplitPane;


import groovy.util.AbstractFactory;
import groovy.util.FactoryBuilderSupport;

/**
 * @author Ben
 *
 */
class OrTermFactory extends AbstractFactory {

    /**
     * {@inheritDoc}
     */
    @Override
    public Object newInstance(FactoryBuilderSupport builder, Object name, Object value, Map attributes)
                throws InstantiationException, IllegalAccessException {
        return new OrTermEx()
    }
                
    public void setChild(FactoryBuilderSupport build, Object parent, Object child) {
        parent.addTerm(child)
    }
    
    class OrTermEx {
        def termsEx = []
        @Delegate OrTerm term
        
        void addTerm(def subTerm) {
            termsEx << subTerm
            term = new OrTerm(termsEx as SearchTerm[])
        }
    }
}
