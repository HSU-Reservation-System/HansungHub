from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
from bs4 import BeautifulSoup
import requests
import datetime

app = FastAPI()
global_session = requests.Session()


# ============================================================
# Request Models
# ============================================================
class LoginRequest(BaseModel):
    student_id: str
    password: str


class ReservationRequest(BaseModel):
    spaceSeq: int
    spaceName: str
    date: str          # YYYY-MM-DD
    timeList: list[str]
    tel: str
    email: str


# ============================================================
# 로그인 API (정상 흐름 적용)
# ============================================================
@app.post("/login")
def login(req: LoginRequest):
    init_url = "https://hansung.ac.kr/cncschool/index.do"
    global_session.get(init_url)

    login_url = "https://hansung.ac.kr/hnuLogin/cncschool/loginProcess.do"
    payload = {
        "siteId": "cncschool",
        "returnUrl": "",
        "referer": "/cncschool/index.do",
        "inputUserId": req.student_id,
        "inputUserPwd": req.password,
    }

    headers = {
        "Content-Type": "application/x-www-form-urlencoded",
        "Origin": "https://hansung.ac.kr",
        "Referer": "https://hansung.ac.kr/cncschool/index.do",
    }

    res = global_session.post(login_url, data=payload, headers=headers, allow_redirects=False)
    redirect_url = res.headers.get("Location", "")

    print("\n===== LOGIN DEBUG =====")
    print("STATUS:", res.status_code)
    print("LOCATION:", redirect_url)
    print("COOKIES:", global_session.cookies.get_dict())
    print("=======================\n")

    if "/index.do" in redirect_url:
        return {"success": True}
    else:
        raise HTTPException(status_code=401, detail="Login failed")


# ============================================================
# 예약 API (JS 동작을 Python에서 직접 재현)
# ============================================================
@app.post("/reserve")
def reserve(req: ReservationRequest):
    if "JSESSIONID" not in global_session.cookies.get_dict():
        raise HTTPException(status_code=401, detail="Not logged in")

    # STEP 1 — 예약 페이지 가져오기
    detail_url = f"https://hansung.ac.kr/resve/cncschool/40/artclRegistView.do?resveSpceSeq={req.spaceSeq}"
    detail_res = global_session.get(detail_url)

    if detail_res.status_code != 200:
        raise HTTPException(status_code=500, detail="Failed to fetch detail page")

    soup = BeautifulSoup(detail_res.text, "html.parser")

    # STEP 2 — hidden 값 스크래핑
    base_form = {}
    hidden_inputs = soup.find_all("input", {"type": "hidden"})
    for inp in hidden_inputs:
        name = inp.get("name")
        value = inp.get("value", "")
        if name:
            base_form[name] = value

    # STEP 3 — select 값 스크래핑
    selects = soup.find_all("select")
    for sel in selects:
        name = sel.get("name")
        if not name:
            continue
        selected = sel.find("option", selected=True)
        if selected:
            base_form[name] = selected.get("value", "")
        else:
            first = sel.find("option")
            if first:
                base_form[name] = first.get("value", "")

    # ============================================================
    # 🔥 핵심 수정 — JS에서 자동으로 채워지는 필드 직접 보정
    # ============================================================

    # HTML 기준: 공간 그룹은 “코딩 라운지” 이며 group=60
    base_form["group"] = "60"                     # JS: select group
    base_form["groupNm"] = "코딩 라운지"          # JS: hidden groupNm
    base_form["resveGroupSeq"] = "60"             # JS: hidden resveGroupSeq

    # 공간 이름은 요청 값 사용
    base_form["spceNm"] = req.spaceName

    # STEP 4 — 요일 계산
    yoils = ["월", "화", "수", "목", "금", "토", "일"]
    dt = datetime.datetime.strptime(req.date, "%Y-%m-%d")
    sel_day = yoils[dt.weekday()]

    # STEP 5 — 사용자 정보 보정
    base_form["userNm"] = "김민서"
    base_form["hakbun"] = req.student_id if hasattr(req, "student_id") else "2171325"

    results = []
    regist_url = "https://hansung.ac.kr/resve/cncschool/40/artclRegist.do"

    # STEP 6 — 시간 반복 예약
    for single_time in req.timeList:
        form_data = base_form.copy()

        form_data["resveDeStr"] = req.date
        form_data["selDay"] = sel_day
        form_data["resveTm"] = single_time
        form_data["telno"] = req.tel
        form_data["email"] = req.email
        form_data["resveSpceSeq"] = str(req.spaceSeq)

        print("\n========== RESERVE REQUEST ==========")
        print("Requesting Time:", single_time)
        print("Form Keys:", list(form_data.keys()))
        print("=====================================")

        res2 = global_session.post(regist_url, data=form_data, allow_redirects=False)

        print("\n---- RESERVE RESPONSE ----")
        print("STATUS:", res2.status_code)
        print("REDIRECT:", res2.headers.get("Location"))
        print("---------------------------\n")

        if res2.status_code == 302:
            results.append({
                "time": single_time,
                "success": True,
                "location": res2.headers.get("Location")
            })
        else:
            with open("reserve_error.html", "w", encoding="utf-8") as f:
                f.write(res2.text)
            results.append({
                "time": single_time,
                "success": False,
                "status": res2.status_code,
                "location": res2.headers.get("Location"),
                "error_file": "reserve_error.html"
            })

    return {"results": results}


# ============================================================
# 공간 목록 조회
# ============================================================
@app.get("/spaces")
def get_spaces():
    url = "https://hansung.ac.kr/cncschool/12195/subview.do"
    res = global_session.get(url)
    soup = BeautifulSoup(res.text, "html.parser")

    space_list = []
    options = soup.find_all("option")

    for op in options:
        if op.get("value") and "호" in op.text:
            space_list.append({
                "spaceSeq": int(op.get("value")),
                "spaceName": op.text.strip()
            })

    return {"spaces": space_list}


# ============================================================
# 비활성 예약 시간 조회
# ============================================================
@app.get("/disabled-times")
def get_disabled_times(date: str, spaceSeq: int):
    url = "https://hansung.ac.kr/resve/cncschool/40/resveList.do"
    payload = {
        "resveSpceSeq": str(spaceSeq),
        "resveDeStr": date
    }

    res = global_session.post(url, data=payload)

    try:
        data = res.json()
        disabled = [item.get("resveTm") for item in data]
    except:
        disabled = []

    return {"disabled": disabled}
