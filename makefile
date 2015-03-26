#
# global makefile
#

GUI = yes

all:
	echo " "; echo " " ; echo ==dim==; $(MAKE) -f makefile_dim all
	echo " "; echo " " ; echo ==utilities==; $(MAKE) -f makefile_util all
	echo " "; echo " " ; echo ==examples==; $(MAKE) -f makefile_examples all
ifneq ($(GUI),no)
	echo " "; echo " " ; echo ==did==; $(MAKE) -f makefile_did all
	echo " "; echo " " ; echo ==webDid==; $(MAKE) -f makefile_webdid all
endif

# Test, if ODIR is set before trying to clean up.
ifneq ($(ODIR),)
clean:
	/bin/rm -f $(ODIR)/*.o core *~
	cd src; /bin/rm -f core *~
	cd src/did; /bin/rm -f core *~
	cd src/examples; /bin/rm -f core *~
	cd src/util; /bin/rm -f core *~

realclean:	clean
		/bin/rm -f $(ODIR)/*
endif
