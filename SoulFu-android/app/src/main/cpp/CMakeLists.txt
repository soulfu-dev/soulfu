cmake_minimum_required(VERSION 3.14) # 3.14+ required for FetchContent
project(soulfu C CXX)

set(CMAKE_C_STANDARD 99)

include(FetchContent)

# Prevent external dependencies from building unused CLI executables or tests
set(BUILD_TESTING OFF CACHE BOOL "" FORCE)
set(BUILD_PROGRAMS OFF CACHE BOOL "" FORCE)

# ==============================================================================
# 1. Fetch Ogg & Vorbis (Audio Decoding)
# ==============================================================================
FetchContent_Declare(
    ogg
    GIT_REPOSITORY https://github.com/xiph/ogg.git
    GIT_TAG        v1.3.5
)
FetchContent_MakeAvailable(ogg)

# Configure paths so Vorbis can find the freshly fetched Ogg headers
set(OGG_INCLUDE_DIR ${ogg_SOURCE_DIR}/include ${ogg_BINARY_DIR}/include CACHE PATH "" FORCE)
set(OGG_LIBRARY ogg CACHE STRING "" FORCE)

FetchContent_Declare(
    vorbis
    GIT_REPOSITORY https://github.com/xiph/vorbis.git
    GIT_TAG        v1.3.7
)
FetchContent_MakeAvailable(vorbis)

# ==============================================================================
# 2. Fetch libjpeg-turbo (JPEG Image Decoding)
# ==============================================================================
include(GNUInstallDirs)
set(CMAKE_INSTALL_DOCDIR "doc" CACHE PATH "" FORCE)

set(ENABLE_STATIC ON CACHE BOOL "" FORCE)
set(ENABLE_SHARED OFF CACHE BOOL "" FORCE)
set(WITH_TURBOJPEG OFF CACHE BOOL "" FORCE)
set(WITH_SIMD OFF CACHE BOOL "" FORCE) # Disables host NASM requirement for Android

FetchContent_Declare(
    jpeg
    GIT_REPOSITORY https://github.com/libjpeg-turbo/libjpeg-turbo.git
    GIT_TAG        2.1.5.1
)
FetchContent_MakeAvailable(jpeg)

# ==============================================================================
# 3. Add Local Subdirectories (SDL2 & gl4es)
# ==============================================================================
if(EXISTS "${CMAKE_CURRENT_SOURCE_DIR}/SDL2")
    add_subdirectory(SDL2)
elseif(EXISTS "${CMAKE_CURRENT_SOURCE_DIR}/SDL")
    add_subdirectory(SDL)
endif()

if(EXISTS "${CMAKE_CURRENT_SOURCE_DIR}/gl4es")
    set(STATICLIB ON CACHE BOOL "" FORCE)
    add_subdirectory(gl4es)
endif()

# ==============================================================================
# 4. Game Target Configuration
# ==============================================================================
set(SRC_FILES ../../../../../../source/soulfu.c)

add_library(soulfu SHARED ${SRC_FILES})

# Include header paths for all native dependencies
target_include_directories(soulfu PRIVATE 
    source
    ${CMAKE_CURRENT_SOURCE_DIR}/SDL2/include
    ${CMAKE_CURRENT_SOURCE_DIR}/SDL/include
    ${CMAKE_CURRENT_SOURCE_DIR}/gl4es/include
    ${ogg_SOURCE_DIR}/include
    ${ogg_BINARY_DIR}/include
    ${vorbis_SOURCE_DIR}/include
    ${jpeg_SOURCE_DIR}
    ${jpeg_BINARY_DIR}
)

# Find Android System Libraries
find_library(log-lib log)
find_library(android-lib android)
find_library(gles-lib GLESv2)

# Link everything into libsoulfu.so
target_link_libraries(soulfu PRIVATE
    SDL2::SDL2
    GL              # gl4es translation layer
    ogg
    vorbis
    vorbisfile
    jpeg-static     # Static library built by libjpeg-turbo
    ${log-lib}
    ${android-lib}
    ${gles-lib}
)