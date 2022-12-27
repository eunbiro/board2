function chkForm() {
	var f = document.frm;
	
	if(f.title.value == "") {
		alert("성명을 입력해주세요!");
		return false;
	}
	
	if(f.user_id.value == "") {
		alert("아이디를 입력해주세요!");
		return false;
	}
	
	f.submit();
}

function chkDelete(board_no) {
	const result = confirm("삭제하시겠습니까?");
	
	if (result) {
		const url = location.origin;	// http:// localhost:8082
		location.href = url + "/board2/delete?board_no=" + board_no;	// 페이지 이동
	} else {
		return false;
	}
}