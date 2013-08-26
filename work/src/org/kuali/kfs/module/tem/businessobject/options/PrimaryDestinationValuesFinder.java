/*
 * Copyright 2012 The Kuali Foundation.
 *
 * Licensed under the Educational Community License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.opensource.org/licenses/ecl1.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kuali.kfs.module.tem.businessobject.options;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.kuali.kfs.module.tem.TemConstants;
import org.kuali.kfs.module.tem.businessobject.PrimaryDestination;
import org.kuali.kfs.module.tem.service.TravelService;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.rice.core.api.util.ConcreteKeyValue;
import org.kuali.rice.core.api.util.KeyValue;
import org.kuali.rice.krad.keyvalues.KeyValuesBase;
import org.kuali.rice.krad.service.BusinessObjectService;

public class PrimaryDestinationValuesFinder extends KeyValuesBase {

    /**
     * @see org.kuali.rice.krad.keyvalues.KeyValuesFinder#getKeyValues()
     */
    @SuppressWarnings("rawtypes")
    @Override
    public List<KeyValue> getKeyValues() {
        List<KeyValue> keyValues = new ArrayList<KeyValue>();
        keyValues.add(new ConcreteKeyValue("", ""));
        BusinessObjectService service = SpringContext.getBean(BusinessObjectService.class);
        TravelService travelService = SpringContext.getBean(TravelService.class);
        Map<String,String> fieldValues = new HashMap<String, String>();

        List<PrimaryDestination> primaryDestinationsInternational = travelService.findAllDistinctPrimaryDestinations(TemConstants.TEMTripTypes.INTERNATIONAL);

        List<PrimaryDestination> primaryDestinationsDomestic = travelService.findAllDistinctPrimaryDestinations(TemConstants.TEMTripTypes.DOMESTIC);


        Iterator<PrimaryDestination> it = primaryDestinationsDomestic.iterator();

        String key = "";
        while (it.hasNext()){
            PrimaryDestination primaryDestination = it.next();

            String tempKey = primaryDestination.getPrimaryDestinationName();
            if (!tempKey.equals(key)){
                keyValues.add(new ConcreteKeyValue(primaryDestination.getPrimaryDestinationName().toUpperCase(), primaryDestination.getPrimaryDestinationName().toUpperCase()));
            }
            key = tempKey;
        }
        keyValues.add(new ConcreteKeyValue("---", "------------------------------------------"));
        it =primaryDestinationsInternational.iterator();
        while (it.hasNext()){
            PrimaryDestination primaryDestination = it.next();
            //skip dummy value for custom expenses

            String tempKey = primaryDestination.getPrimaryDestinationName();
            if (!tempKey.equals(key)){
                keyValues.add(new ConcreteKeyValue(primaryDestination.getPrimaryDestinationName().toUpperCase(), primaryDestination.getPrimaryDestinationName().toUpperCase()));
            }
            key = tempKey;
        }


        return keyValues;
    }
}
