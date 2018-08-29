package com.fasthink.shalemonitor.action;

import javax.ws.rs.core.Response;

public interface DataAccessAction
{
	String fetchHistoryData(String parm);

	Response exportExcel(String parm);
}
