# web-crawler in Python and search engine in Java

#### Python VirtualEnv Python 3.4
Run following command in terminal:
```
pip install virtualenv virtualenvwrapper
virtualenv --python=python3.4 ~/vp34 
```
This will create a “vp34” directory in your home directory. To activate this virtualenv, run:
```
source ~/vp34/bin/activate
```
You can check the python version by this command:
```
python --version 
```

#### install requirements 
```
$ cd stack
$ pip install -r requirements.txt
```
#### Generate a requirements file.
```
$ pip freeze > requirements.txt
```
#### run crawler
Run the following command within the “stack” directory:
```
$ scrapy crawl stack_crawler
```

#### check scraped data
cd to `stack/scraped-data/` directory to see the scraped data with `.json` extension


