/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.hpccsystems.javaecl;

/**
 *
 * @author ChalaAX
 */
public class SaltDataProfilingAPI implements EclCommand {

    private String name;
    private String datasetName;
    private String layout;
    private String saltLib;
    private String recordName;


    public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDatasetName() {
		return datasetName;
	}

	public void setDatasetName(String datasetName) {
		this.datasetName = datasetName;
	}

	public String getLayout() {
		return layout;
	}

	public void setLayout(String layout) {
		this.layout = layout;
	}
	
	

	public String getSaltLib() {
		return saltLib;
	}

	public void setSaltLib(String saltLib) {
		//this.saltLib = saltLib;
		this.saltLib = saltLib.replaceAll("[^A-Za-z0-9]", "");
	}
	
	

	public String getRecordName() {
		return recordName;
	}

	public void setRecordName(String recordName) {
		this.recordName = recordName;
	}

	@Override
    public String ecl() {
		//String regex = "[ ]";
		String ecl = "";
		String unique = this.name.replaceAll("[^A-Za-z0-9]", "");//String unique = this.name.replace(" ", "_");
		String inDataset = this.datasetName;
		inDataset = "in_" + this.datasetName;
		
		String idFix = "";
		
		idFix += this.saltLib + ".layout_" + this.layout + " SpoonTransform(" + this.datasetName +" L) := TRANSFORM\r\n";
		idFix += " SELF.spoonClusterID := 0;\r\n";
		idFix += " SELF.spoonRecordID := 0;\r\n";
		idFix += " SELF := L;\r\n";
		idFix += "END;\r\n\r\n";
		idFix += inDataset + " := project(" + this.datasetName + ",SpoonTransform(LEFT));\r\n\r\n";
		
		
		idFix += this.saltLib + ".layout_" + this.layout + " AddIds("+ inDataset +" L,"+ inDataset +" R) := TRANSFORM\r\n";
		idFix += " SELF.spoonClusterID := L.spoonRecordID + 1;\r\n";
		idFix += " SELF.spoonRecordID := L.spoonRecordID + 1;\r\n";
		idFix += " SELF := R;\r\n";
		idFix += "END;\r\n\r\n";
		idFix += "out_" + this.datasetName + " := ITERATE("+inDataset+",AddIds(LEFT,RIGHT));\r\n\r\n";
		
		ecl += idFix;
		inDataset = "out_" + this.datasetName;
		
        ecl += "h_" + unique + " := " + saltLib + ".Hygiene(" + inDataset+ ");\r\n";
        ecl += "p_" + unique + " := h_" + unique + ".AllProfiles;\r\n";
        ecl += "//output data\r\n";
        ecl += "OUTPUT(h_" + unique + ".Summary('SummaryReport'), NAMED('Dataprofiling_SummaryReport'), ALL);\r\n";
        ecl += "OUTPUT(SALT25.MAC_Character_Counts.EclRecord(p_" + unique + ", '" + this.layout + "'),NAMED('Dataprofiling_OptimizedLayout'));\r\n";
        ecl += "OUTPUT(p_" + unique + ", NAMED('Dataprofiling_AllProfiles'), ALL);\r\n";
        
       
        return ecl;
    }

    @Override
    public CheckResult check() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
