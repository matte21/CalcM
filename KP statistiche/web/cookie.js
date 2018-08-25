function scriviCookie(nome,valore,durata){//minuti
	var scadenza=new Date();
	scadenza.setTime(new Date().getTime()+parseInt(durata*60000));
	document.cookie=nome+'='+escape(valore)+'; expires='+scadenza.toGMTString()+'; path=/';
}
function leggiCookie(nome){
	if (document.cookie.length>0){
		var inizio=document.cookie.indexOf(nome+"=");
		if (inizio!=-1){
			inizio=inizio+nome.length+1;
			var fine=document.cookie.indexOf(";",inizio);
			if (fine==-1)
				fine=document.cookie.length;
			return unescape(document.cookie.substring(inizio,fine));
		} else
			return "";
	}
	return "";
}
function cancellaCookie(nome){
	scriviCookie(nome,"",-1);
}