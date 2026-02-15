import requests
from bs4 import BeautifulSoup

from login import login

sess = login("21250213227", "123456Aa..")

URL = "https://pgs.ouc.edu.cn/py/page/student/grkcgl.htm"

# COOKIES = {
#     "JSESSIONID": "9055356104B8F0022BFF7D5E8A6055F1",
#     "SSO_TGC": "TGT-9950694-cLhAABnsU4Iwta00N-ZW29pw7JLDpsvwZgE1jzg9wN-4QdyvduVSqSlqlJd17wDSIZWp1UxkCFHLqmB",
# }

HEADERS = {
    "User-Agent": (
        "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) "
        "AppleWebKit/537.36 (KHTML, like Gecko) "
        "Chrome/144.0.0.0 Safari/537.36"
    ),
    "Referer": "https://pgs.ouc.edu.cn/",
    "Accept-Language": "zh-CN,zh;q=0.9",
}


CSS_SELECTOR = (
    "body > div.layout_main.br5 > div.setting_module.pt10 > "
    "div > div > div > div:nth-child(11) > div.xkmid"
)


def main():
    r = requests.get(
        URL,
        headers=HEADERS,
        cookies=sess.cookies,
        timeout=15
    )
    r.raise_for_status()

    soup = BeautifulSoup(r.text, "lxml")

    el = soup.select_one(CSS_SELECTOR)
    if not el:
        raise RuntimeError("未找到目标元素（可能未登录或页面结构变化）")

    text = el.get_text(strip=True)
    print(text)


if __name__ == "__main__":
    main()
