// tiff stream interface class implementation

#include "tiffstream.h"

const char *TiffStream::m_name = "TiffStream";

TiffStream::TiffStream() {
    m_tif = NULL;


    m_inStream = NULL;
    m_outStream = NULL;
    m_ioStream = NULL;

    m_streamLength = 0;

    m_this = reinterpret_cast<thandle_t>(this);
};

TiffStream::~TiffStream() {
    if (m_tif != NULL) TIFFClose(m_tif);
}

TIFF *
TiffStream::makeFileStream(istream *str) {
    m_inStream = str;
    m_outStream = NULL;
    m_ioStream = NULL;
    m_streamLength = getSize(m_this);

    m_tif = TIFFClientOpen(m_name,
                           "r",
                           m_this,
                           read,
                           write,
                           seek,
                           close,
                           size,
                           map,
                           unmap);
    return m_tif;
}

TIFF *
TiffStream::makeFileStream(ostream *str) {
    m_inStream = NULL;
    m_outStream = str;
    m_ioStream = NULL;
    m_streamLength = getSize(m_this);

    m_tif = TIFFClientOpen(m_name,
                           "w",
                           m_this,
                           read,
                           write,
                           seek,
                           close,
                           size,
                           map,
                           unmap);
    return m_tif;
}

TIFF *
TiffStream::makeFileStream(iostream *str) {
    m_inStream = NULL;
    m_outStream = NULL;
    m_ioStream = str;
    m_streamLength = getSize(m_this);

    m_tif = TIFFClientOpen(m_name,
                           "r+w",
                           m_this,
                           read,
                           write,
                           seek,
                           close,
                           size,
                           map,
                           unmap);
    return m_tif;
}

tsize_t
TiffStream::read(thandle_t fd, tdata_t buf, tsize_t size) {
    istream *istr;
    TiffStream *ts = reinterpret_cast<TiffStream *>(fd);
    if (ts->m_inStream != NULL) {
        istr = ts->m_inStream;
    } else if (ts->m_ioStream != NULL) {
        istr = ts->m_ioStream;
    }

    int remain = ts->m_streamLength - ts->tell(fd);
    int actual = remain < size ? remain : size;
    istr->read(reinterpret_cast<char *>(buf), actual);
    if (TIFF_DEBUG) {
        LOGI("======TiffStream::read actual==%d", actual);
        LOGI("======TiffStream::read buf==%ld", *((char *) buf));
    }
    return istr->gcount();
}

tsize_t
TiffStream::write(thandle_t fd, tdata_t buf, tsize_t size) {
    TiffStream *ts = reinterpret_cast<TiffStream *>(fd);
    ostream *ostr;
    if (ts->m_outStream != NULL) {
        ostr = ts->m_outStream;
    } else if (ts->m_ioStream != NULL) {
        ostr = ts->m_ioStream;
    }

    streampos start = ostr->tellp();
    ostr->write(reinterpret_cast<const char *>(buf), size);
    return ostr->tellp() - start;
}

toff_t
TiffStream::seek(thandle_t fd, toff_t offset, int origin) {
    if (TIFF_DEBUG) {
        LOGI("=====TiffStream::seek offset==%u,origin==%d", offset, origin);
    }
    TiffStream *ts = reinterpret_cast<TiffStream *>(fd);
    if (ts->seekInt(fd, offset, origin) == true) return offset;
    else return -1;
}

int
TiffStream::close(thandle_t fd) {
    TiffStream *ts = reinterpret_cast<TiffStream *>(fd);
    if (ts->m_inStream != NULL) {
        ts->m_inStream = NULL;
        return 0;
    } else if (ts->m_outStream != NULL) {
        ts->m_outStream = NULL;
        return 0;
    } else if (ts->m_ioStream != NULL) {
        ts->m_ioStream = NULL;
        return 0;
    }
    return -1;
}

toff_t
TiffStream::size(thandle_t fd) {
    TiffStream *ts = reinterpret_cast<TiffStream *>(fd);
    return ts->getSize(fd);
}

int
TiffStream::map(thandle_t fd, tdata_t *phase, toff_t *psize) {
    return 0;
}

void
TiffStream::unmap(thandle_t fd, tdata_t base, toff_t size) {
}

unsigned int
TiffStream::getSize(thandle_t fd) {
    if (!isOpen(fd)) return 0;

    unsigned int pos = tell(fd);
    seekInt(fd, 0, end);
    unsigned int size = tell(fd);
    seekInt(fd, pos, beg);
    if (TIFF_DEBUG) LOGI("=====TiffStream::getSize size==%ud", size);
    return size;
}

unsigned int
TiffStream::tell(thandle_t fd) {
    TiffStream *ts = reinterpret_cast<TiffStream *>(fd);
    if (ts->m_inStream != NULL) {
        return ts->m_inStream->tellg();
    } else if (ts->m_outStream != NULL) {
        return ts->m_outStream->tellp();
    } else if (ts->m_ioStream != NULL) {
        return ts->m_ioStream->tellg();
    }
    return 0;
}

bool
TiffStream::seekInt(thandle_t fd, unsigned int offset, int origin) {
    if (TIFF_DEBUG) {
        LOGI("=====TiffStream::seekInt offset==%u,origin==%d", offset, origin);
    }
    if (!isOpen(fd)) return false;

    ios::seekdir org;
    switch (origin) {
        case beg:
            org = ios::beg;
            break;
        case cur:
            org = ios::cur;
            break;
        case end:
            org = ios::end;
            break;
    }

    bool isSuccess = false;

    TiffStream *ts = reinterpret_cast<TiffStream *>(fd);
    if (ts->m_inStream != NULL) {
        ts->m_inStream->seekg(offset, (ios_base::seekdir) org);
        isSuccess = true;
    } else if (ts->m_outStream != NULL) {
        ts->m_outStream->seekp(offset, (ios_base::seekdir) org);
        isSuccess = true;
    } else if (ts->m_ioStream != NULL) {
        ts->m_ioStream->seekg(offset, (ios_base::seekdir) org);
        ts->m_ioStream->seekp(offset, (ios_base::seekdir) org);
        isSuccess = true;
    }

    if (TIFF_DEBUG) {
        int remain = ts->m_streamLength - ts->tell(fd);
        LOGI("=====TiffStream::seekInt remain==%u", remain);
    }

    return isSuccess;
}

bool
TiffStream::isOpen(thandle_t fd) {
    TiffStream *ts = reinterpret_cast<TiffStream *>(fd);
    return (ts->m_inStream != NULL ||
            ts->m_outStream != NULL ||
            ts->m_ioStream != NULL);
}/*
 * Local Variables:
 * mode: c++
 * c-basic-offset: 8
 * fill-column: 78
 * End:
 */