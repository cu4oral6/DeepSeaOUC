import re
import requests

LOGIN_PAGE = (
    "https://id.ouc.edu.cn:8071/sso/login"
    "?service=https%3A%2F%2Fpgs.ouc.edu.cn%2Fallogene%2Fpage%2Fhome.htm"
)

LOGIN_POST = LOGIN_PAGE


def get_flow_id(session: requests.Session) -> str:
    resp = session.get(LOGIN_PAGE, timeout=15)
    resp.raise_for_status()

    m = re.search(
        r'<input[^>]*name=["\']flowId["\'][^>]*value=["\']([^"\']+)["\']',
        resp.text,
        re.I
    )
    if not m:
        raise RuntimeError("未找到 flowId")

    return m.group(1)


def login(username: str, password: str) -> requests.Session:
    session = requests.Session()

    # 模拟浏览器 UA
    session.headers.update({
        "User-Agent": (
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) "
            "AppleWebKit/537.36 (KHTML, like Gecko) "
            "Chrome/144.0.0.0 Safari/537.36"
        ),
        "Accept-Language": "zh-CN,zh;q=0.9",
    })

    # 1️⃣ 先 GET，拿 JSESSIONID + flowId
    flow_id = get_flow_id(session)
    print("flowId =", flow_id)

    # 2️⃣ POST 登录
    data = {
        "username": username,
        "password": password,
        "submit": "登录",
        "loginType": "username_password",
        "flowId": flow_id,
        "captcha": "",
        "delegator": "",
        "tokenCode": "",
        "continue": "",
        "asserts": "",
        "pageFrom": "",
    }

    headers = {
        "Origin": "https://id.ouc.edu.cn:8071",
        "Referer": LOGIN_PAGE,
        "Content-Type": "application/x-www-form-urlencoded",
    }

    resp = session.post(
        LOGIN_POST,
        data=data,
        headers=headers,
        allow_redirects=True,
        timeout=15
    )

    resp.raise_for_status()
    # print('status_code =', resp.status_code)
    # print('headers =', resp.headers)
    # print('text =', resp.text)
    return session


if __name__ == "__main__":
    sess = login("21250***", "******")

    print("\n====== 当前 Session Cookies ======")
    for c in sess.cookies:
        print(c.name, "=", c.value)


    # 测试访问目标系统
    test = sess.get(
        "https://pgs.ouc.edu.cn/allogene/page/home.htm",
        allow_redirects=True
    )
    print("\n目标系统状态码:", test.status_code)
