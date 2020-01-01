# Copyright (C) 2019 Björn Lindqvist <bjourne@gmail.com>
from os.path import splitext

def options(ctx):
    ctx.load('java')

def configure(ctx):
    ctx.load('java')

def build(ctx):
    ctx(features = 'javac',
        srcdir = 'src/')
