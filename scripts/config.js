require.config({
    baseUrl: './scripts',
    paths: {
      jquery: 'jquery-1.8.3',
      jqueryui: 'ui/jquery-ui-1.9.2.custom',
      prettify: 'prettify/run_prettify',
      bootstrap: 'bootstrap/js/bootstrap',
      scrollAdjust: 'scroll-adjust',
      tocCreator: 'toc-creator',
      setup: 'setup'
    },
    shim: {
        jqueryui: { deps: ['jquery'] },
        bootstrap: { deps: ['jquery'] },
    }
});

// Load the main app module to start the app
// requirejs(['jquery', 'jqueryui', 'scrollAdjust', 'bootstrap', 'SyntaxHighlight', 'shJava', 'setup'], function($) {
requirejs(['jquery', 'jqueryui', 'scrollAdjust', 'bootstrap', 'prettify', 'tocCreator', 'setup'], function($, hljs) {

	    $(document).ready( function($) {
            $(function() {
                $( '#accordion' ).accordion( {
                    autoHeight: false,
                    header: 'h3',
                    collapsible: true,
                    active: false
                } );
                $( '#whats-new-accordion' ).accordion( { autoHeight: false } );

            });

            setup();
        }, false );
	});