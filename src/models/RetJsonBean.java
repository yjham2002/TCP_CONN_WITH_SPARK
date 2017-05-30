package models;

public class RetJsonBean
{
    private int		returnCode;
    private String	returnMessage;
    private Object	entity;
    private Object	addData;

    public int getReturnCode()
    {
        return returnCode;
    }

    public void setReturnCode(int returnCode)
    {
        this.returnCode = returnCode;
    }

    public String getReturnMessage()
    {
        return returnMessage;
    }

    public void setReturnMessage(String returnMessage)
    {
        this.returnMessage = returnMessage;
    }

    public Object getEntity()
    {
        return entity;
    }

    public void setEntity(Object entity)
    {
        this.entity = entity;
    }

    public Object getAddData()
    {
        return addData;
    }

    public void setAddData(Object addData)
    {
        this.addData = addData;
    }

}
