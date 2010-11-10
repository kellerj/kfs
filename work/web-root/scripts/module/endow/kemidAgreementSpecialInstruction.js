/*
 * Copyright 2007 The Kuali Foundation.
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
 */

 function loadAgreementSpecialInstructionCodeDesc(codeFieldName){
	var elPrefix = findElPrefix(codeFieldName.name);
	var codeDescriptionFieldName = elPrefix + ".agreementSpecialInstruction.name";
	var code = DWRUtil.getValue(codeFieldName);
	
	setCodeDescription(code, codeDescriptionFieldName);
}

function setCodeDescription(code, codeDescriptionFieldName){
	 
alert(code + "-" + codeDescriptionFieldName);

 	if (code == '') {
 		clearRecipients(codeDescriptionFieldName);
 	} else {
 		var dwrReply = {
 			callback:function(data) {
 				if ( data != null && typeof data == 'object') {
 					setRecipientValue(codeDescriptionFieldName, data.name);
 				} else {
 					setRecipientValue(codeDescriptionFieldName, wrapError("Code not found"), true);			
 				} },
 			errorHandler:function(errorMessage) { 
 				setRecipientValue(codeDescriptionFieldName, wrapError("Code not found"), true);
 			}
 		};
 		AgreementSpecialInstructionService.getByPrimaryKey(code.toUpperCase(), dwrReply);		
 	}
}
 

 
 


