 
Ext.require(['*']);

Ext.onReady(function(){


    var randomNumber = Math.floor(Math.random()*10000001);

    Ext.QuickTips.init();

    var storeNodes = Ext.create('Ext.data.TreeStore', {
        proxy: {
            type: 'rest',
            url: '/smiData.json',
	    extraParams: {
		browser: randomNumber
	    },
            reader: {
                type: 'json',
                root: 'children'
            }
        },
 	autoLoad: false,
        folderSort: true,
        sorters: [{
            property: 'text',
            direction: 'ASC'
        }],
        root: {
            text: 'TopNode',
            id: 'src',
            expanded: true
        },
        listeners: {
		load: function(myStore, node, records, successful, eopts) {
			setTimeout(expandRoot, 100);
		}
	}
     });

    function expandRoot()
    {
	var node;

    	node = storeNodes.getRootNode();
	node.expandChildren();
    }

    var storeServices = Ext.create('Ext.data.TreeStore', {
        proxy: {
            type: 'rest',
//            url: '/didServices.json',
            url: '/smiObjects.json',
 	    extraParams: {
		browser: randomNumber
	    },
            reader: {
                type: 'json',
                root: 'children'
            }
        },
	fields: ['name', 'state', 'mode'],
        folderSort: true,
//	sortRoot: 'name',
        sorters: [{
            property: 'name',
            direction: 'ASC'
        }],
        root: {
            text: 'TopNode',
            id: 'src',
            expanded: true
        },
        listeners: {
		load: function(myStore, node, records, successful, eopts) {
			node.expand(1);
		}
        }	
    });

    function clearServices()
    {
	storeServices.load();
	serviceInfo.setVisible(1);
	commandInfoArea.setVisible(0);
	serviceInfo.update("");
    }
 
    var serviceInfo = Ext.create('Ext.Panel', {
        layout: 'fit',
        id: 'details-panel',
        title: 'Object Info',
        width: 580,
	flex: 3,
        autoScroll: true,
	border: false,
        html: ''
    });

    var commandInfo = Ext.create('Ext.Panel', {
        layout: 'fit',
        width: 580,
	border: false,
        margin: '5 5 5 5',
        html: 'Some Info'
    });


    var commandData = Ext.create('Ext.form.field.Text', {
        width: 400,
	xtype: 'textfield',
	name: 'Search pattern',
	labelAlign: 'top',
	fieldLabel: "Please enter items separated by spaces:<br />(for example: 2 0x123 'A' 23.4 \"a text\")",
	labelPad: 5,
	labelSeparator: "",
        margin: '5 5 5 5'
    });

    var commandButton = Ext.create('Ext.Button', {
        text: 'Send',
        margin: '5 5 5 5',
	handler: function() {
	    var pattern = commandData.getRawValue();
	    query(pattern, -1);	
	}
    });
	
    var commandInfoArea = Ext.create('Ext.form.Panel', {
        layout: 'vbox',
        id: 'cmd-details-panel',
        title: 'Send Command',
        width: 580,
	border: false,
	align:'stretch',
	items: [commandInfo, commandData, commandButton]	
    });

    var serviceInfoArea = Ext.create('Ext.form.Panel', {
        layout: 'vbox',
        width: 580,
        region: 'east',
	align:'stretch',
	items: [serviceInfo, commandInfoArea]	
    });

    var HTTPPacket = new XMLHttpRequest();
    HTTPPacket.onreadystatechange = process;
    var HTTPPollPacket = new XMLHttpRequest();
    HTTPPollPacket.onreadystatechange = pollAnswer;
    var HTTPQueryPacket = new XMLHttpRequest();
    HTTPQueryPacket.onreadystatechange = queryAnswer;
    var requestNumber = 0;
    var LastService = "";
    var LastId = "";    
    var forceUpdate = 0;
    var timeoutid;
    var pollid;
    var CurrService = "";
    var OldNServices = 0;
    var OldNServers = 0;
    var OldNnodes = 0;
    var OldNSearch = -1;
    var OldNDomains = 0;
    var CurrSmiDomain = "";
    var CurrSmiDomainParentId;
    var CurrSmiDomainId;
    function poll()
    {
	requestNumber = requestNumber + 1;
	HTTPPollPacket.open( "GET", "/didPoll.json/src?dimservice="+CurrService+"&reqNr="+requestNumber+"&reqId="+randomNumber+"&force=0", true ); 
	HTTPPollPacket.send( null );
    }
    function pollAnswer()
    {
	var answer;
	if ( HTTPPollPacket.readyState != 4 )
		return;
	answer = HTTPPollPacket.responseText;
console.log("got answer "+answer);
	if(answer == "")
	{
		pollid = setTimeout(poll, 5000);
		return;
	}
	var items = answer.split(" ");
	if((items[1] != OldNServers) || (items[2] != OldNnodes))
	{
	    storeNodes.load();
	    clearServices();
	    CurrSmiDomain = "";
	}
	if(items[5] != OldNDomains)
	{
	    headerList.update(items[5]+" SMI Domains Known (on "+items[6]+" nodes)");
	} 
	OldNServers = items[1];
	OldNnodes = items[2];
	OldNDomains = items[5];
	if((items[3] > 0) && (CurrSmiDomain != ""))
	{
		    storeServices.reload({
		        params: {
		            dimserver: CurrSmiDomain ,
		            dimnode: CurrSmiDomainParentId,
			    dimserverid: CurrSmiDomainId
			}
		    });
	}
/*
	if((items[0] != OldNServices) || (items[1] != OldNServers))
	{
	    headerList.update(items[1]+" Servers Known - "+items[0]+" Services Available (on "+items[2]+" nodes)");
	}
	if((items[1] != OldNServers) || (items[2] != OldNnodes))
	{
	    storeNodes.load();
	    clearServices();
	}
	if(items[3] != 0)
	    doGetService();
	OldNServices = items[0];
	OldNServers = items[1];
	OldNnodes = items[2];
	if(items[4] != OldNSearch)
	{
	    if(items[4] != 0)
	        searchInfo.update("Showing: "+items[5]+" Servers - "+items[4]+" Services (on "+items[6]+" nodes)");
	    else
	        searchInfo.update("Showing: All");
	    OldNSearch = items[4];
	}
*/
	pollid = setTimeout(poll, 2000);
    }

    function query(pattern, force)
    {
	if(pollid)
		clearTimeout(pollid);
	pollid = 0;
	requestNumber = requestNumber + 1;
	HTTPQueryPacket.open( "GET", "/didQuery.json/src?dimservice="+pattern+"&reqNr="+requestNumber+"&reqId="+randomNumber+"&force="+force, true ); 
	HTTPQueryPacket.send( null );
    }
    function queryAnswer()
    {
	var answer;
	if ( HTTPQueryPacket.readyState != 4 )
		return;
	answer = HTTPQueryPacket.responseText;
	if(answer == "load")
	{
		storeNodes.load(); 
		clearServices();
//		poll();
	}
//	else
//		pollid = setTimeout(poll, 1000);
    } 

    function doGetService()
    {
	getService(LastService, LastId);
    }
    function getService(name, id)
    {
	if(pollid)
		clearTimeout(pollid);
	pollid = 0;
	forceUpdate = 0;
	if(LastService != name)
	{
		forceUpdate = 1;
		LastService = name;
		LastId = id;
	}
	var items = id.split("|");
	if(items.length == 3)
	{
		commandInfo.update(name + " is a DIM Command");
		CurrService = "";
		forceUpdate = -1;
	}
	var name1 = name.replace(/\?/g,"%3F");
	name1 = name1.replace(/\&/g,"%26");
	if(forceUpdate != -1)
		CurrService = name1;
	requestNumber = requestNumber + 1;
	HTTPPacket.open( "GET", "/didServiceData.json/src?dimservice="+name1+"&id=src&reqNr="+requestNumber+"&reqId="+randomNumber+"&force="+forceUpdate, true ); 
	HTTPPacket.send( null );
    }
    function process() 
    {
	serviceInfo.update("Updating - state "+HTTPPacket.readyState+"...");
	if ( HTTPPacket.readyState != 4 )
		return; 
	if(HTTPPacket.responseText == "")
	{
		pollid = setTimeout(poll, 5000);
		return;
	}
	if(forceUpdate != -1)
	{
		serviceInfo.update(HTTPPacket.responseText);
		serviceInfo.setVisible(1);
		commandInfoArea.setVisible(0);
	}
	else
	{
		commandInfo.update(HTTPPacket.responseText);
		serviceInfo.setVisible(0);
		commandInfoArea.setVisible(1);
	}
	pollid = setTimeout(poll, 5000);
    } 

    var serviceList = Ext.create('Ext.tree.Panel', {
        title: 'Objects',
        width: 360,
        height: 150,
        store: storeServices,
        rootVisible: false,
	autoScroll: true,
//        multiSelect: true,
//        singleExpand: true,
        //the 'columns' property is now 'headers'
        columns: [{
            xtype: 'treecolumn', //this is so we know which column will show the tree
            text: 'Name',
            flex: 2,
            sortable: true,
            dataIndex: 'name'
        },{
//	    xtype: 'actioncolumn',
            text: 'State',
//            flex: 1,
            width: 120,
            dataIndex: 'state',
            sortable: true
        },{
//	    xtype: 'actioncolumn',
            text: 'Mode',
//            flex: 1,
            width: 100,
            dataIndex: 'mode',
            sortable: true
        }],
        listeners: {
	    itemclick: function(view, rec, item, index, evtObj) {
		getService(rec.get('name'),rec.get('id'));
	    }
        }	
    });

    var nodeTree = Ext.create('Ext.tree.Panel', {
        title: 'Nodes & Domains',
        width: 360,
        height: 150,
        store: storeNodes,
        rootVisible: false,
	autoScroll: true,
        listeners: {
	    itemclick: function(view, rec, item, index, evtObj) {
		if(rec.get('leaf') == true)
		{
    		    CurrSmiDomain = rec.get('name');
    		    CurrSmiDomainParentId = rec.get('parentId');
    		    CurrSmiDomainId = rec.get('id');
//console.log("Selected Domain "+CurrSmiDomain);
		    storeServices.load({
		        params: {
		            dimserver: CurrSmiDomain ,
		            dimnode: CurrSmiDomainParentId,
			    dimserverid: CurrSmiDomainId
			}
		    });
		}
	    }
        }	
    });

    var headerList = Ext.create('Ext.Panel', {
        layout: 'fit',
        title: 'SMI++ Display',
        id: 'top-panel',
        width: 2000,
        height: 55,
        bodyPadding: '5 0 5 5',
        html: ''
    });

    var searchButton = Ext.create('Ext.Button', {
        text: 'Search',
        margin: '2 0 5 5',
	handler: function() {
		clearServices();
		var pattern = inputArea.getRawValue();
		query(pattern, 0);	
	}
    });

    var allButton = Ext.create('Ext.Button', {
        text: 'Show All',
        margin: '2 0 5 5',
	handler: function() {
		clearServices();
		var pattern = "";
		query(pattern, 0);	
	}
    });
	
    var searchInfo = Ext.create('Ext.Panel', {
        layout: 'fit',
        id: 'search-panel',
        margin: '5 0 5 10',
	border: 0,
        html: ''
    });

    var inputArea = Ext.create('Ext.form.field.Text', {
        width: 400,
        id: 'pattern',
	xtype: 'textfield',
	name: 'Search pattern',
	fieldLabel: 'pattern',
	labelWidth: 45,
        margin: '2 0 5 5'
    });

    Ext.create('Ext.Viewport', {
        layout: 'border',
        title: 'DID',
        items: [{
            layout: 'vbox',
            id: 'layout-browser3',
            region:'north',
            border: false,
            split:true,
            margins: '2 0 5 5',
            items: [headerList]
        },{
            layout: 'fit',
            id: 'layout-browser',
            region:'west',
            border: false,
            split:true,
            margins: '2 0 5 5',
            width: 220,
            minSize: 100,
            maxSize: 500,
            items: [nodeTree]
        },{
            layout: 'fit',
            id: 'layout-browser1',
            region:'center',
            border: false,
            split:true,
            margins: '2 0 5 5',
            minSize: 100,
            maxSize: 500,
            items: [serviceList]
        },{
            layout: 'fit',
            id: 'layout-browser2',
            region:'east',
            border: false,
            split:true,
            margins: '2 0 5 5',
//            width: 580,
            width: 480,
            minSize: 100,
            maxSize: 600,
            items: [serviceInfoArea]
        }
        ],
        renderTo: Ext.getBody()
    });
    commandInfoArea.setVisible(0);
//console.log("polling... ");
    poll();
});

