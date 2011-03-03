# coding: utf-8
import os
from wsgiref.util import FileWrapper

import cloudstorage.settings
from cloudstorage.db import get_db_connection

FILE_STORE='/tmp/cloud'

class FileSystemException(Exception):
    def __init__(self, text, code=500):
        self.text = text
        self.code = code
    def __str__(self):
        return '%s %s' % (repr(self.text), repr(self.code))

def check_write_enabler(storage_index, write_enabler):
    db_connection = get_db_connection()
    cursor = db_connection.cursor()
    cursor.execute('SELECT * FROM write_enablers WHERE storage_index = %s' % (
                    storage_index))
    result = cursor.fetchone()
    if result is not None:
        return result == write_enabler
    else:
        raise FileSystemException(u'File does not have storage index, is it an'
                                  u'immutable file?', 403)

    return False

def get_path(storage_index):
    path = os.path.join(FILE_STORE, storage_index)
    if os.path.exists(path):
        return path
    else:
        return None

def save_file(storage_index, data, write_enabler=None):
    if storage_index is not None:
        try:
            fd = os.open(os.path.join(FILE_STORE, storage_index),
                                      os.O_WRONLY | os.O_CREAT | os.O_EXCL)
            fp = os.fdopen(fd, 'w')
        except OSError:
            # The file already exist
            if check_write_enabler(storage_index, write_enabler):
                fp = open(os.path.join(FILE_STORE, storage_index), 'w')
            else:
                raise FileSystemException('Wrong write-enabler', 401)

        fp.write(data)
        fp.close()

        return True

    raise FileSystemException('Missing storage index')

def retrieve_file(storage_index):
    if storage_index is not None:
        path = get_path(storage_index)
        if path is not None:
            file_iterator = FileWrapper(open(path, 'rb'))
            size = os.path.getsize(path)
            return (file_iterator, size)

    raise FileSystemException('Could not find file.', 400)
