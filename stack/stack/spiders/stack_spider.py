from scrapy import Spider
from scrapy.selector import Selector

from stack.items import StackItem
import re

class StackSpider(Spider):
    name = "stack_spider"
    allowed_domains = ["stackoverflow.com"]
    start_urls = [
        "http://stackoverflow.com/questions?pagesize=50&sort=newest",
    ]

    def parse(self, response):
        summarys = response.xpath('//div[@class="summary"]')

        for summary in summarys:
            item = StackItem()
            item['url'] = 'http://stackoverflow.com' + summary.xpath(
                'h3/a[@class="question-hyperlink"]/@href').extract()[0]
            item['title'] = summary.xpath(
                'h3/a[@class="question-hyperlink"]/text()').extract()[0]
            s = summary.xpath(
                'div[@class="excerpt"]/text()').extract()[0]
            # replace all punctuations from the plain text with space, and devide them by space
            item['excerpt'] = re.sub('[^A-Za-z0-9]+', ' ', s).strip()

            yield item

